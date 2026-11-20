package com.sspl.designsystem.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme

/** Theme-aware aliases (light + dark). Prefer AppTheme.colors directly in new code. */
object SsplColorTokens {
    val primary: Color @Composable get() = AppTheme.colors.primary
    val primaryDark: Color @Composable get() = AppTheme.colors.primaryDark
    val tertiary: Color @Composable get() = AppTheme.colors.tertiary
    val accent: Color @Composable get() = AppTheme.colors.accent
    val secondary: Color @Composable get() = AppTheme.colors.secondary
    val primaryLight: Color @Composable get() = AppTheme.colors.primaryLight
    val surface: Color @Composable get() = AppTheme.colors.surface
    val background: Color @Composable get() = AppTheme.colors.background
    val surfaceTint: Color @Composable get() = AppTheme.colors.surfaceTint
    val boxColor: Color @Composable get() = AppTheme.colors.boxColor
    val border: Color @Composable get() = AppTheme.colors.border
    val divider: Color @Composable get() = AppTheme.colors.divider
    val heading: Color @Composable get() = AppTheme.colors.heading
    val body: Color @Composable get() = AppTheme.colors.body
    val textPrimary: Color @Composable get() = AppTheme.colors.heading
    val textSecondary: Color @Composable get() = AppTheme.colors.body
    val textMuted: Color @Composable get() = AppTheme.colors.muted
    val muted: Color @Composable get() = AppTheme.colors.muted
    val success: Color @Composable get() = AppTheme.colors.success
    val successContainer: Color @Composable get() = AppTheme.colors.successContainer
    val warning: Color @Composable get() = AppTheme.colors.warning
    val warningContainer: Color @Composable get() = AppTheme.colors.warningContainer
    val error: Color @Composable get() = AppTheme.colors.error
    val errorContainer: Color @Composable get() = AppTheme.colors.errorContainer
}

object SsplSpacingTokens {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
}

object SsplTypographyTokens {
    val displayLarge: TextStyle @Composable get() = AppTheme.typography.displaySmall
    val headlineLarge: TextStyle @Composable get() = AppTheme.typography.headlineMedium
    val headlineMedium: TextStyle @Composable get() = AppTheme.typography.headlineMedium
    val headlineSmall: TextStyle @Composable get() = AppTheme.typography.headlineSmall
    val titleLarge: TextStyle @Composable get() = AppTheme.typography.titleLarge
    val titleMedium: TextStyle @Composable get() = AppTheme.typography.titleMedium
    val titleSmall: TextStyle @Composable get() = AppTheme.typography.titleSmall
    val bodyLarge: TextStyle @Composable get() = AppTheme.typography.bodyLarge
    val bodyMedium: TextStyle @Composable get() = AppTheme.typography.bodyMedium
    val bodySmall: TextStyle @Composable get() = AppTheme.typography.bodySmall
    val caption: TextStyle @Composable get() = AppTheme.typography.labelSmall
    val labelLarge: TextStyle @Composable get() = AppTheme.typography.labelMedium
    val labelMedium: TextStyle @Composable get() = AppTheme.typography.labelMedium
    val labelSmall: TextStyle @Composable get() = AppTheme.typography.labelSmall
}
