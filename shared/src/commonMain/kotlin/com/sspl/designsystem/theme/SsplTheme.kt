package com.sspl.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.theme.AppFontFamily

@Immutable
data class SsplColorTokens(
    val primary: Color = Color(0xFF0A528F),
    val primaryDark: Color = Color(0xFF053762),
    val tertiary: Color = Color(0xFF032746),
    val accent: Color = Color(0xFF0095B8),
    val secondary: Color = Color(0xFFF5B041),
    val primaryLight: Color = Color(0xFF0876A8),
    val surface: Color = Color(0xFFFFFFFF),
    val background: Color = Color(0xFFF6F9FC),
    val surfaceTint: Color = Color(0xFFEAF5FA),
    val boxColor: Color = Color(0xFFEBF1F6),
    val border: Color = Color(0xFFD8E5ED),
    val heading: Color = Color(0xFF19364D),
    val body: Color = Color(0xFF374B5E),
    val muted: Color = Color(0xFF617487),
    val success: Color = Color(0xFF1E7B34),
    val successContainer: Color = Color(0xFFE8F5E9),
    val warning: Color = Color(0xFFC27705),
    val warningContainer: Color = Color(0xFFFFF3E0),
    val error: Color = Color(0xFFB3261E),
    val errorContainer: Color = Color(0xFFFFEBEE),
    val onPrimary: Color = Color.White,
    val onSurface: Color = Color(0xFF19364D),
    val onBackground: Color = Color(0xFF374B5E),
    val white: Color = Color.White
)

@Immutable
data class SsplSpacingTokens(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val screenHorizontal: Dp = 16.dp,
    val sectionSpacing: Dp = 20.dp
)

@Immutable
data class SsplShapeTokens(
    val card: RoundedCornerShape = RoundedCornerShape(16.dp),
    val button: RoundedCornerShape = RoundedCornerShape(12.dp),
    val chip: RoundedCornerShape = RoundedCornerShape(50),
    val banner: RoundedCornerShape = RoundedCornerShape(16.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val small: RoundedCornerShape = RoundedCornerShape(8.dp),
    val tiny: RoundedCornerShape = RoundedCornerShape(4.dp)
)

@Immutable
data class SsplTypographyTokens(
    val displaySmall: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle
)

val LocalSsplColors = staticCompositionLocalOf { SsplColorTokens() }
val LocalSsplSpacing = staticCompositionLocalOf { SsplSpacingTokens() }
val LocalSsplShapes = staticCompositionLocalOf { SsplShapeTokens() }
val LocalSsplTypography = staticCompositionLocalOf<SsplTypographyTokens> {
    error("No SsplTypography provided")
}

object AppTheme {
    val colors: SsplColorTokens
        @Composable
        get() = LocalSsplColors.current

    val spacing: SsplSpacingTokens
        @Composable
        get() = LocalSsplSpacing.current

    val shapes: SsplShapeTokens
        @Composable
        get() = LocalSsplShapes.current

    val typography: SsplTypographyTokens
        @Composable
        get() = LocalSsplTypography.current
}

fun ssplMaterialColorScheme(tokens: SsplColorTokens): ColorScheme {
    return lightColorScheme(
        primary = tokens.primary,
        onPrimary = Color.White,
        primaryContainer = tokens.surfaceTint,
        onPrimaryContainer = tokens.primaryDark,
        secondary = tokens.primaryDark,
        onSecondary = Color.White,
        tertiary = tokens.accent,
        onTertiary = Color.White,
        background = tokens.background,
        onBackground = tokens.heading,
        surface = tokens.surface,
        onSurface = tokens.heading,
        surfaceVariant = tokens.boxColor,
        onSurfaceVariant = tokens.body,
        outline = tokens.border,
        error = tokens.error,
        onError = Color.White,
        errorContainer = tokens.errorContainer,
        onErrorContainer = tokens.error
    )
}

@Composable
fun SsplTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = SsplColorTokens()
    val spacing = SsplSpacingTokens()
    val shapes = SsplShapeTokens()
    val fontFamily = AppFontFamily()

    val typographyTokens = SsplTypographyTokens(
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = colors.heading
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colors.heading
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = colors.heading
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = colors.heading
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = colors.heading
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.body
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = colors.body
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = colors.muted
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = colors.muted
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            letterSpacing = 0.6.sp,
            color = colors.muted
        )
    )

    val materialTypography = Typography(
        displaySmall = typographyTokens.displaySmall,
        titleLarge = typographyTokens.titleLarge,
        titleMedium = typographyTokens.titleMedium,
        bodyLarge = typographyTokens.bodyLarge,
        bodyMedium = typographyTokens.bodyMedium,
        labelMedium = typographyTokens.labelMedium,
        labelSmall = typographyTokens.labelSmall
    )

    val materialShapes = Shapes(
        small = shapes.small,
        medium = shapes.card,
        large = shapes.sheet
    )

    CompositionLocalProvider(
        LocalSsplColors provides colors,
        LocalSsplSpacing provides spacing,
        LocalSsplShapes provides shapes,
        LocalSsplTypography provides typographyTokens
    ) {
        MaterialTheme(
            colorScheme = ssplMaterialColorScheme(colors),
            typography = materialTypography,
            shapes = materialShapes,
            content = content
        )
    }
}

