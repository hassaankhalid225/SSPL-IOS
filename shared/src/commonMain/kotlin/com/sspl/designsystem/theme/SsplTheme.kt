package com.sspl.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.theme.AppFontFamily

/**
 * SSPL colour system — light + dark.
 *
 * Brand family stays SSPL blue. Palette:
 *  Ink Navy  #061E3A · SSPL Blue #0B5AA6 · Cobalt #1A73D9 · Lancet Cyan #14A6CC
 * Amber is reserved for "pending" status only (never a brand accent).
 *
 * Token names are kept from v1 so every screen keeps compiling; new tokens are additive.
 */
@Immutable
data class SsplColorTokens(
    val isDark: Boolean = false,
    // Brand
    val primary: Color = Color(0xFF0B5AA6),
    val primaryDark: Color = Color(0xFF061E3A),
    val tertiary: Color = Color(0xFF04172E),
    val accent: Color = Color(0xFF14A6CC),
    /** Former yellow. Now a soft cobalt used for secondary emphasis. */
    val secondary: Color = Color(0xFF1A73D9),
    val primaryLight: Color = Color(0xFF1A73D9),
    val primaryContainer: Color = Color(0xFFE6F0FB),
    val onPrimaryContainer: Color = Color(0xFF0A3F75),
    // Surfaces
    val surface: Color = Color(0xFFFFFFFF),
    val surfaceElevated: Color = Color(0xFFFFFFFF),
    val background: Color = Color(0xFFF3F6FA),
    val surfaceTint: Color = Color(0xFFEAF2FB),
    val boxColor: Color = Color(0xFFF0F4F9),
    val border: Color = Color(0xFFE2E9F2),
    val divider: Color = Color(0xFFEDF1F6),
    // Text
    val heading: Color = Color(0xFF0D1B2C),
    val body: Color = Color(0xFF3A4A5E),
    val muted: Color = Color(0xFF6B7B90),
    // Status
    val success: Color = Color(0xFF1B7F45),
    val successContainer: Color = Color(0xFFE5F5EC),
    val warning: Color = Color(0xFFB86E00),
    val warningContainer: Color = Color(0xFFFFF2DE),
    val error: Color = Color(0xFFC0362C),
    val errorContainer: Color = Color(0xFFFDECEA),
    val info: Color = Color(0xFF0B7FA6),
    val infoContainer: Color = Color(0xFFE3F5FA),
    // On-colours
    val onPrimary: Color = Color.White,
    val onSurface: Color = Color(0xFF0D1B2C),
    val onBackground: Color = Color(0xFF3A4A5E),
    val white: Color = Color.White,
    // Skeleton
    val skeletonBase: Color = Color(0xFFE6ECF3),
    val skeletonHighlight: Color = Color(0xFFF6F9FC),
    // Header gradient stops (used by headerGradient)
    val gradientStart: Color = Color(0xFF061E3A),
    val gradientMid: Color = Color(0xFF0B4A8C),
    val gradientEnd: Color = Color(0xFF1A73D9),
    /** Card shadow tint. */
    val shadow: Color = Color(0xFF0B2A4A),
    // Filled actions (buttons, selected pills). Separate from `primary` so dark mode can use a
    // soft text-level blue for links/icons and a deeper solid blue under white button text.
    val buttonStart: Color = Color(0xFF0B5AA6),
    val buttonEnd: Color = Color(0xFF1A73D9),
    /** Eyebrow / greeting colour on gradient heroes. */
    val heroAccent: Color = Color(0xFF9FDCF0),
    /** Pill / badge sitting on a gradient hero. */
    val heroChip: Color = Color(0xFFFFFFFF),
    val onHeroChip: Color = Color(0xFF0B4A8C),
    /** Strength of decorative rings on gradient surfaces (0..1). */
    val decorAlpha: Float = 1f
) {
    val textMuted: Color get() = muted

    /** Diagonal navy → cobalt. Used by headers, hero cards, splash. */
    val headerGradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(gradientStart, gradientMid, gradientEnd),
            start = Offset(0f, 0f),
            end = Offset(1400f, 900f)
        )

    /** Vertical variant for tall areas (drawer header, auth hero). */
    val headerGradientVertical: Brush
        get() = Brush.verticalGradient(listOf(gradientStart, gradientMid, gradientEnd))

    /** Bright button gradient. */
    val primaryGradient: Brush
        get() = Brush.horizontalGradient(listOf(buttonStart, buttonEnd))

    /** Subtle tinted card fill. */
    val softGradient: Brush
        get() = Brush.linearGradient(listOf(surfaceTint, surface))
}

val LightSsplColors = SsplColorTokens()

/**
 * Dark palette — neutral cool greys (not navy), soft text-level blue, deep solid blue fills,
 * no glow. Surfaces step up in lightness for elevation instead of relying on shadows.
 */
val DarkSsplColors = SsplColorTokens(
    isDark = true,
    // Brand / actions
    primary = Color(0xFF8DB8F2),          // links, icons, selected text
    primaryDark = Color(0xFF1B2533),
    tertiary = Color(0xFF0B0F14),
    accent = Color(0xFF86C5D8),           // muted teal, used sparingly
    secondary = Color(0xFFA9C6EC),
    primaryLight = Color(0xFFA9C6EC),
    primaryContainer = Color(0xFF1C2735),
    onPrimaryContainer = Color(0xFFCFE0F6),
    buttonStart = Color(0xFF2C67AD),
    buttonEnd = Color(0xFF3471B8),
    // Surfaces (elevation = lighter)
    background = Color(0xFF0E1116),
    surface = Color(0xFF161B22),
    surfaceElevated = Color(0xFF1D232C),
    surfaceTint = Color(0xFF1A212B),
    boxColor = Color(0xFF1B2129),
    border = Color(0xFF262D37),
    divider = Color(0xFF1F252E),
    // Text
    heading = Color(0xFFE7EBF0),
    body = Color(0xFFB3BCC8),
    muted = Color(0xFF808B99),
    // Status (desaturated for dark)
    success = Color(0xFF6CC495),
    successContainer = Color(0xFF15261D),
    warning = Color(0xFFE1AE5B),
    warningContainer = Color(0xFF2A2215),
    error = Color(0xFFEE867D),
    errorContainer = Color(0xFF2D1A19),
    info = Color(0xFF86C5D8),
    infoContainer = Color(0xFF15242A),
    // On-colours
    onPrimary = Color(0xFF0E1116),
    onSurface = Color(0xFFE7EBF0),
    onBackground = Color(0xFFB3BCC8),
    // Skeleton
    skeletonBase = Color(0xFF1B2129),
    skeletonHighlight = Color(0xFF242B35),
    // Header: quiet deep blue-grey, no neon
    gradientStart = Color(0xFF111821),
    gradientMid = Color(0xFF15202D),
    gradientEnd = Color(0xFF1B2C42),
    heroAccent = Color(0xFF9DB9DA),
    heroChip = Color(0xFF243245),
    onHeroChip = Color(0xFFDCE7F5),
    decorAlpha = 0.45f,
    shadow = Color(0xFF000000)
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
    val sectionSpacing: Dp = 22.dp
)

@Immutable
data class SsplShapeTokens(
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val button: RoundedCornerShape = RoundedCornerShape(14.dp),
    val chip: RoundedCornerShape = RoundedCornerShape(50.dp),
    val banner: RoundedCornerShape = RoundedCornerShape(20.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val small: RoundedCornerShape = RoundedCornerShape(12.dp),
    val tiny: RoundedCornerShape = RoundedCornerShape(6.dp),
    /** Bottom edge of gradient headers. */
    val header: RoundedCornerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
)

@Immutable
data class SsplTypographyTokens(
    val displaySmall: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle = titleMedium,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    /** Wide-tracked caps for section eyebrows. */
    val overline: TextStyle = labelSmall,
    /** Big numbers in stat tiles. */
    val number: TextStyle = headlineMedium
)

private val safeDefaultTypographyTokens = SsplTypographyTokens(
    displaySmall = TextStyle(fontSize = 24.sp),
    headlineMedium = TextStyle(fontSize = 20.sp),
    headlineSmall = TextStyle(fontSize = 17.sp),
    titleLarge = TextStyle(fontSize = 18.sp),
    titleMedium = TextStyle(fontSize = 15.sp),
    titleSmall = TextStyle(fontSize = 14.sp),
    bodyLarge = TextStyle(fontSize = 14.sp),
    bodyMedium = TextStyle(fontSize = 13.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelMedium = TextStyle(fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 10.5.sp)
)

val LocalSsplColors = staticCompositionLocalOf { LightSsplColors }
val LocalSsplSpacing = staticCompositionLocalOf { SsplSpacingTokens() }
val LocalSsplShapes = staticCompositionLocalOf { SsplShapeTokens() }
val LocalSsplTypography = staticCompositionLocalOf<SsplTypographyTokens> {
    safeDefaultTypographyTokens
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

    val isDark: Boolean
        @Composable
        get() = LocalSsplColors.current.isDark
}

fun ssplMaterialColorScheme(tokens: SsplColorTokens): ColorScheme {
    return if (tokens.isDark) {
        darkColorScheme(
            primary = tokens.primary,
            onPrimary = tokens.onPrimary,
            primaryContainer = tokens.primaryContainer,
            onPrimaryContainer = tokens.onPrimaryContainer,
            secondary = tokens.secondary,
            onSecondary = tokens.onPrimary,
            secondaryContainer = tokens.surfaceTint,
            onSecondaryContainer = tokens.heading,
            tertiary = tokens.accent,
            onTertiary = tokens.onPrimary,
            background = tokens.background,
            onBackground = tokens.heading,
            surface = tokens.surface,
            onSurface = tokens.heading,
            surfaceVariant = tokens.boxColor,
            onSurfaceVariant = tokens.body,
            surfaceContainer = tokens.surface,
            surfaceContainerLow = tokens.surface,
            surfaceContainerHigh = tokens.surfaceElevated,
            surfaceContainerHighest = tokens.surfaceElevated,
            surfaceContainerLowest = tokens.background,
            outline = tokens.border,
            outlineVariant = tokens.divider,
            error = tokens.error,
            onError = Color.White,
            errorContainer = tokens.errorContainer,
            onErrorContainer = tokens.error,
            inverseSurface = Color(0xFFE7EBF0),
            inverseOnSurface = Color(0xFF161B22),
            inversePrimary = Color(0xFF2C67AD),
            scrim = Color(0xCC000000)
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            onPrimary = Color.White,
            primaryContainer = tokens.primaryContainer,
            onPrimaryContainer = tokens.onPrimaryContainer,
            secondary = tokens.secondary,
            onSecondary = Color.White,
            secondaryContainer = tokens.surfaceTint,
            onSecondaryContainer = tokens.heading,
            tertiary = tokens.accent,
            onTertiary = Color.White,
            background = tokens.background,
            onBackground = tokens.heading,
            surface = tokens.surface,
            onSurface = tokens.heading,
            surfaceVariant = tokens.boxColor,
            onSurfaceVariant = tokens.body,
            surfaceContainer = tokens.surface,
            surfaceContainerLow = tokens.surface,
            surfaceContainerHigh = tokens.surface,
            surfaceContainerHighest = tokens.boxColor,
            surfaceContainerLowest = tokens.surface,
            outline = tokens.border,
            outlineVariant = tokens.divider,
            error = tokens.error,
            onError = Color.White,
            errorContainer = tokens.errorContainer,
            onErrorContainer = tokens.error,
            inverseSurface = Color(0xFF1C2633),
            inverseOnSurface = Color(0xFFEEF2F7),
            inversePrimary = Color(0xFF8DB8F2)
        )
    }
}

@Composable
fun SsplTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkSsplColors else LightSsplColors
    val spacing = remember { SsplSpacingTokens() }
    val shapes = remember { SsplShapeTokens() }
    val fontFamily = AppFontFamily()
    val typographyTokens = ssplTypography(fontFamily, colors)

    // Material components (Snackbar, dialogs, menus, buttons, text fields) take their text colour
    // from LocalContentColor. Baked-in colours here would override that — e.g. dark body text on a
    // dark snackbar. So Material gets colour-free styles; AppTheme.typography keeps the colours.
    fun TextStyle.noColor() = copy(color = Color.Unspecified)
    val materialTypography = Typography(
        displaySmall = typographyTokens.displaySmall.noColor(),
        headlineMedium = typographyTokens.headlineMedium.noColor(),
        headlineSmall = typographyTokens.headlineSmall.noColor(),
        titleLarge = typographyTokens.titleLarge.noColor(),
        titleMedium = typographyTokens.titleMedium.noColor(),
        titleSmall = typographyTokens.titleSmall.noColor(),
        bodyLarge = typographyTokens.bodyLarge.noColor(),
        bodyMedium = typographyTokens.bodyMedium.noColor(),
        bodySmall = typographyTokens.bodySmall.noColor(),
        labelLarge = typographyTokens.labelMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold).noColor(),
        labelMedium = typographyTokens.labelMedium.noColor(),
        labelSmall = typographyTokens.labelSmall.noColor()
    )

    val materialShapes = Shapes(
        extraSmall = shapes.tiny,
        small = shapes.small,
        medium = shapes.card,
        large = shapes.sheet,
        extraLarge = shapes.sheet
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

private fun ssplTypography(fontFamily: FontFamily, colors: SsplColorTokens) = SsplTypographyTokens(
    displaySmall = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp, color = colors.heading
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp, color = colors.heading
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 23.sp, color = colors.heading
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp, color = colors.heading
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 21.sp, color = colors.heading
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 19.sp, color = colors.heading
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 21.sp, color = colors.body
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 19.sp, color = colors.body
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp, color = colors.muted
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, color = colors.muted
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 0.4.sp, color = colors.muted
    ),
    overline = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp, color = colors.accent
    ),
    number = TextStyle(
        fontFamily = fontFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.4).sp, color = colors.heading
    )
)
