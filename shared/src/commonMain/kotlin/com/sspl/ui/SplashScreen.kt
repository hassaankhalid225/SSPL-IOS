package com.sspl.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.repositories.AuthRepository
import com.sspl.core.usecases.GetUserDetailsUseCase
import com.sspl.designsystem.theme.AppTheme
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import com.sspl.session.UserSession
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

/**
 * A1: Branded Splash Screen with session check & auto-route.
 */
@Composable
fun SplashScreen(
    navController: NavController,
    userSession: UserSession = koinInject(),
    authRepository: AuthRepository = koinInject(),
    getUserDetailsUseCase: GetUserDetailsUseCase = koinInject()
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo entrance
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        val token = userSession.token
        val refreshToken = userSession.refreshToken

        if (!token.isNullOrBlank()) {
            // Check session validity with getMe()
            try {
                val meResponse = authRepository.getMe()
                if (meResponse.status.isSuccess()) {
                    // Refresh local user profile
                    getUserDetailsUseCase().collect { state ->
                        if (state is ApiStates.Success && state.data != null) {
                            userSession.setUser(state.data)
                        }
                    }
                    delay(300)
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.SplashScreen.route) { inclusive = true }
                    }
                    return@LaunchedEffect
                } else if (meResponse.status == HttpStatusCode.Unauthorized && !refreshToken.isNullOrBlank()) {
                    // Try silent token refresh
                    try {
                        val refreshResponse = authRepository.refreshToken(refreshToken)
                        if (refreshResponse.status.isSuccess()) {
                            val userResp = refreshResponse.body<com.sspl.core.models.UserResponse>()
                            userResp.accessToken?.let { userSession.setToken(it) }
                            userResp.refreshToken?.let { userSession.setRefreshToken(it) }
                            userResp.user?.let { userSession.setUser(it) }

                            delay(300)
                            navController.navigate(Screen.MainScreen.route) {
                                popUpTo(Screen.SplashScreen.route) { inclusive = true }
                            }
                            return@LaunchedEffect
                        }
                    } catch (_: Exception) {
                        // Refresh failed, proceed to clear session
                    }
                }
            } catch (_: Exception) {
                // Network error or timeout: if token is cached, allow user into MainScreen
                delay(400)
                navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
                return@LaunchedEffect
            }

            // If we got here and response wasn't success, clear and go to login
            userSession.resetToken()
            userSession.resetUser()
            delay(300)
            navController.navigate(Screen.SignInScreen.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
        } else {
            // No session saved
            delay(1000)
            navController.navigate(Screen.SignInScreen.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "SSPL Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Society of Surgeons of Pakistan",
                style = AppTheme.typography.titleLarge.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lahore Chapter · Advancing Surgical Excellence",
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = AppTheme.colors.secondary,
                strokeWidth = 3.dp
            )
        }

        Text(
            text = "SSPL • v1.0.0",
            style = AppTheme.typography.labelSmall.copy(
                color = AppTheme.colors.muted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
