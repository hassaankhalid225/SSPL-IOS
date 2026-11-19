package com.sspl.designsystem.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme

object SsplColorTokens {
    val primary: Color = Color(0xFF0A528F)
    val primaryDark: Color = Color(0xFF053762)
    val tertiary: Color = Color(0xFF032746)
    val accent: Color = Color(0xFF0095B8)
    val secondary: Color = Color(0xFFF5B041)
    val primaryLight: Color = Color(0xFF0876A8)
    val surface: Color = Color(0xFFFFFFFF)
    val background: Color = Color(0xFFF6F9FC)
    val surfaceTint: Color = Color(0xFFEAF5FA)
    val boxColor: Color = Color(0xFFEBF1F6)
    val border: Color = Color(0xFFD8E5ED)
    val divider: Color = Color(0xFFD8E5ED)
    val heading: Color = Color(0xFF19364D)
    val body: Color = Color(0xFF374B5E)
    val textPrimary: Color = Color(0xFF19364D)
    val textSecondary: Color = Color(0xFF374B5E)
    val textMuted: Color = Color(0xFF617487)
    val muted: Color = Color(0xFF617487)
    val success: Color = Color(0xFF1E7B34)
    val successContainer: Color = Color(0xFFE8F5E9)
    val warning: Color = Color(0xFFB57406)
    val warningContainer: Color = Color(0xFFFFF3CD)
    val error: Color = Color(0xFFC62828)
    val errorContainer: Color = Color(0xFFFFEBEE)
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
