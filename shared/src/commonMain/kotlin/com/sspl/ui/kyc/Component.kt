package com.sspl.ui.kyc

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.DecorativeRings
import com.sspl.designsystem.components.GlassIconButton
import com.sspl.designsystem.theme.AppTheme
import com.sspl.designsystem.theme.SystemBarsAppearance
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import org.jetbrains.compose.resources.painterResource

/* ==========================================================================================
 * Auth shell shared by sign-in, sign-up, OTP, forgot password and onboarding (KYC) screens.
 * These screens live OUTSIDE the main shell, so they own their insets:
 *  - navy gradient hero drawn behind the status bar (statusBarsPadding on hero content)
 *  - surface sheet with 28dp top corners overlapping the hero by 24dp
 *  - the whole page scrolls, and pads for IME + navigation bar at the bottom
 * ======================================================================================== */

private val SheetOverlap = 24.dp

@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    onBack: (() -> Unit)? = null,
    eyebrow: String? = null,
    heroFraction: Float = 0.38f,
    content: @Composable ColumnScope.() -> Unit
) {
    SystemBarsAppearance(lightStatusIcons = true, darkTheme = AppTheme.isDark)
    val colors = AppTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
    ) {
        val heroHeight = (maxHeight * heroFraction).coerceIn(250.dp, 340.dp)
        val sheetMinHeight = (maxHeight - heroHeight + SheetOverlap).coerceAtLeast(0.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
                )
                .verticalScroll(rememberScrollState())
        ) {
            Box(Modifier.fillMaxWidth()) {
                AuthHero(
                    title = title,
                    subtitle = subtitle,
                    eyebrow = eyebrow,
                    onBack = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeight)
                )
                Column(
                    modifier = Modifier
                        .padding(top = heroHeight - SheetOverlap)
                        .fillMaxWidth()
                        .heightIn(min = sheetMinHeight)
                        .clip(AppTheme.shapes.sheet)
                        .background(colors.surface)
                        .padding(horizontal = 22.dp, vertical = 26.dp),
                    content = content
                )
            }
        }

        if (snackbarHostState != null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(
                        WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
                    )
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun AuthHero(
    title: String,
    subtitle: String,
    eyebrow: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(AppTheme.colors.headerGradient)) {
        DecorativeRings(Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = SheetOverlap + 14.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(44.dp)) {
                if (onBack != null) {
                    GlassIconButton(
                        icon = SsplIcons.ArrowBack,
                        contentDescription = "Go back",
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LogoDisc()
                Spacer(Modifier.height(14.dp))
                if (eyebrow != null) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = AppTheme.typography.overline.copy(color = Color.White.copy(alpha = 0.78f))
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = title,
                    style = AppTheme.typography.headlineMedium.copy(color = Color.White),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.76f)),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

/** White disc holding the SSPL crest. Only used on gradient surfaces. */
@Composable
fun LogoDisc(size: androidx.compose.ui.unit.Dp = 76.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .padding(size * 0.13f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = "SSPL logo",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** Divider with a centred label ("or"). */
@Composable
fun LabeledDivider(label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = AppTheme.colors.divider)
        Text(
            text = label,
            style = AppTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = AppTheme.colors.divider)
    }
}

/** "New to SSPL? Create account" style footer with a 48dp touch target on the action. */
@Composable
fun AuthFooterLink(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = prompt, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted))
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(AppTheme.shapes.tiny)
                .clickable(onClick = onClick)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = action,
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

/** Small heading inside the sheet ("Sign in", "Account details"). */
@Composable
fun SheetHeading(title: String, caption: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = title, style = AppTheme.typography.titleLarge.copy(color = AppTheme.colors.heading))
        if (caption != null) {
            Spacer(Modifier.height(2.dp))
            Text(text = caption, style = AppTheme.typography.bodySmall)
        }
    }
}
