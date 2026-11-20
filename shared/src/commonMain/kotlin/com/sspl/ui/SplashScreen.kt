package com.sspl.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.repositories.AuthRepository
import com.sspl.core.usecases.GetUserDetailsUseCase
import com.sspl.designsystem.components.LoadingDots
import com.sspl.designsystem.theme.AppTheme
import com.sspl.designsystem.theme.SystemBarsAppearance
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import com.sspl.session.UserSession
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private enum class SplashDestination { MAIN, SIGN_IN }

/**
 * A1: Animated splash with session check.
 *
 * Routing rules (fixes "app opens as guest"):
 *  - real member token  → validate (max 5 s; offline = trust cache) → Main
 *  - guest session      → Sign-in screen (guest is one tap away and reuses the same session)
 *  - nothing / invalid  → Sign-in screen
 */
@Composable
fun SplashScreen(
    navController: NavController,
    userSession: UserSession = koinInject(),
    authRepository: AuthRepository = koinInject(),
    getUserDetailsUseCase: GetUserDetailsUseCase = koinInject()
) {
    SystemBarsAppearance(lightStatusIcons = true, darkTheme = true)

    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val ringSweep = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(18f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            // 1) Intro choreography (~1.4 s) runs alongside the session check.
            val intro = async {
                launch { logoAlpha.animateTo(1f, tween(450)) }
                logoScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 220f))
                launch { ringSweep.animateTo(360f, tween(900, easing = FastOutSlowInEasing)) }
                delay(350)
                launch { textOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
                textAlpha.animateTo(1f, tween(500))
                delay(250)
            }
            val destination = async { resolveDestination(userSession, authRepository, getUserDetailsUseCase) }

            intro.await()
            val target = destination.await()
            navController.navigate(
                if (target == SplashDestination.MAIN) Screen.MainScreen.route else Screen.SignInScreen.route
            ) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
        }
    }

    val colors = AppTheme.colors
    val ambient = rememberInfiniteTransition(label = "ambient")
    val drift by ambient.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF041327), Color(0xFF07294D), Color(0xFF0D4F96)),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 2000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient concentric rings + soft glow (very low contrast).
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height * 0.44f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF2E8BE6).copy(alpha = 0.28f), Color.Transparent),
                    center = c,
                    radius = size.minDimension * (0.55f + 0.05f * drift)
                ),
                radius = size.minDimension * 0.8f,
                center = c
            )
            for (i in 1..4) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.035f * (5 - i)),
                    radius = size.minDimension * (0.18f + i * 0.11f + drift * 0.015f),
                    center = c,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp).padding(bottom = 40.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(168.dp)) {
                // The "suture" ring drawing itself around the logo.
                Canvas(modifier = Modifier.size(168.dp)) {
                    val stroke = 3.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = Color.White.copy(alpha = 0.10f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF3CC6E6), Color(0xFF8FD8FF), Color.White, Color(0xFF3CC6E6))
                        ),
                        startAngle = -90f, sweepAngle = ringSweep.value, useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value; scaleY = logoScale.value; alpha = logoAlpha.value
                        }
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.app_logo),
                        contentDescription = "SSPL logo",
                        modifier = Modifier.size(104.dp)
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffset.value.dp.toPx()
                }
            ) {
                Text(
                    text = "SSPL",
                    color = Color.White,
                    fontFamily = AppTheme.typography.displaySmall.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = 10.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Society of Surgeons of Pakistan",
                    style = AppTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.92f)),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "LAHORE CHAPTER",
                    style = AppTheme.typography.overline.copy(color = colors.accent.copy(alpha = 0.95f), letterSpacing = 3.sp),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
                .graphicsLayer { alpha = textAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoadingDots(color = Color.White.copy(alpha = 0.85f), dotSize = 6.dp)
            Text(
                text = "Advancing surgical excellence",
                style = AppTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

private suspend fun resolveDestination(
    userSession: UserSession,
    authRepository: AuthRepository,
    getUserDetailsUseCase: GetUserDetailsUseCase
): SplashDestination {
    val token = userSession.token
    if (token.isNullOrBlank()) return SplashDestination.SIGN_IN

    // Guest sessions never auto-enter: the user sees sign-in first and can tap "Continue as guest".
    if (userSession.userFlow.value?.isGuest() == true) return SplashDestination.SIGN_IN

    val outcome = withTimeoutOrNull(5_000) {
        try {
            val me = authRepository.getMe()
            when {
                me.status.isSuccess() -> {
                    // Refresh cached profile in the background of the splash (non-fatal).
                    runCatching {
                        getUserDetailsUseCase().collect { state ->
                            if (state is ApiStates.Success && state.data != null) userSession.setUser(state.data)
                        }
                    }
                    SplashDestination.MAIN
                }
                me.status == HttpStatusCode.Unauthorized -> {
                    val refresh = userSession.refreshToken
                    if (!refresh.isNullOrBlank()) {
                        runCatching {
                            val r = authRepository.refreshToken(refresh)
                            if (r.status.isSuccess()) {
                                val body = r.body<com.sspl.core.models.UserResponse>()
                                body.accessToken?.let { userSession.setToken(it) }
                                body.refreshToken?.let { userSession.setRefreshToken(it) }
                                body.user?.let { userSession.setUser(it) }
                                SplashDestination.MAIN
                            } else SplashDestination.SIGN_IN
                        }.getOrNull() ?: SplashDestination.SIGN_IN
                    } else SplashDestination.SIGN_IN
                }
                else -> SplashDestination.MAIN // 5xx etc: don't punish the user, trust cached session
            }
        } catch (_: Exception) {
            SplashDestination.MAIN // offline: trust cached session
        }
    }

    return when (outcome) {
        SplashDestination.MAIN -> SplashDestination.MAIN
        // Timed out on a slow network: trust the cached member session rather than block launch.
        null -> if (userSession.userFlow.value != null) SplashDestination.MAIN else SplashDestination.SIGN_IN
        SplashDestination.SIGN_IN -> SplashDestination.SIGN_IN
    }.also { dest ->
        if (dest == SplashDestination.SIGN_IN) {
            userSession.resetToken()
            userSession.resetUser()
        }
    }
}
