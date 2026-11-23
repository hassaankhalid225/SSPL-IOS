package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme

/* ------------------------------------------------------------------------------------------
 * Buttons. Loading state shows LoadingDots — never a circular spinner.
 * Every button gets a subtle press-scale so taps feel physical.
 * ---------------------------------------------------------------------------------------- */

/** Base pressable used by all buttons. */
@Composable
private fun ButtonFrame(
    modifier: Modifier,
    background: Brush?,
    borderColor: Color?,
    enabled: Boolean,
    busy: Boolean,
    minHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = 600f), label = "btnScale")
    val shape = AppTheme.shapes.button
    var m = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .defaultMinSize(minHeight = minHeight)
        .clip(shape)
    if (background != null) m = m.background(background)
    if (borderColor != null) m = m.border(1.5.dp, borderColor, shape)
    Box(
        modifier = m
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(),
                enabled = enabled && !busy,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            content = content
        )
    }
}

@Composable
private fun RowScope.ButtonContent(
    text: String,
    busy: Boolean,
    contentColor: Color,
    icon: ImageVector?,
    trailingIcon: ImageVector?
) {
    if (busy) {
        LoadingDots(color = contentColor, dotSize = 6.dp, spacing = 5.dp)
    } else {
        if (icon != null) Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium.copy(color = contentColor, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (trailingIcon != null) Icon(trailingIcon, null, tint = contentColor, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    backgroundColor: Color? = null
) {
    val busy = loading || isLoading
    val colors = AppTheme.colors
    val bg: Brush = when {
        !enabled -> SolidColor(colors.border)
        backgroundColor != null -> SolidColor(backgroundColor)
        else -> colors.primaryGradient
    }
    val contentColor = if (enabled) Color.White else colors.muted
    ButtonFrame(modifier.fillMaxWidth(), bg, null, enabled, busy, 52.dp, onClick) {
        ButtonContent(text, busy, contentColor, icon, trailingIcon)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val busy = loading || isLoading
    val colors = AppTheme.colors
    val contentColor = if (enabled) colors.primary else colors.muted
    ButtonFrame(
        modifier.fillMaxWidth(),
        SolidColor(if (enabled) colors.primaryContainer else colors.boxColor),
        null, enabled, busy, 50.dp, onClick
    ) {
        ButtonContent(text, busy, contentColor, icon, null)
    }
}

/** Outlined, transparent. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    contentColor: Color = AppTheme.colors.primary
) {
    val c = if (enabled) contentColor else AppTheme.colors.muted
    ButtonFrame(
        modifier.fillMaxWidth(), null,
        if (enabled) contentColor.copy(alpha = 0.55f) else AppTheme.colors.border,
        enabled, loading, 50.dp, onClick
    ) {
        ButtonContent(text, loading, c, icon, null)
    }
}

@Composable
fun TextButtonLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = AppTheme.colors.primary
) {
    Text(
        text = text,
        style = AppTheme.typography.bodyMedium.copy(
            color = if (enabled) color else AppTheme.colors.muted,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(AppTheme.shapes.tiny)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp)
    )
}

/** Pinned bottom CTA bar. Owns the navigation-bar inset. */
@Composable
fun StickyCtaBar(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    priceText: String? = null,
    priceSubtext: String? = null,
    loading: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surface,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.spacing.screenHorizontal, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (priceText != null) {
                Column(modifier = Modifier.weight(1f)) {
                    if (priceSubtext != null) {
                        Text(
                            text = priceSubtext,
                            style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.muted)
                        )
                    }
                    Text(
                        text = priceText,
                        style = AppTheme.typography.number.copy(color = AppTheme.colors.heading, fontSize = AppTheme.typography.titleLarge.fontSize)
                    )
                }
            }
            if (secondaryText != null && onSecondaryClick != null) {
                SecondaryButton(text = secondaryText, onClick = onSecondaryClick, modifier = Modifier.weight(1f))
            }
            PrimaryButton(
                text = primaryText,
                onClick = onPrimaryClick,
                loading = loading,
                enabled = enabled,
                trailingIcon = SsplIcons.ArrowForward,
                modifier = if (priceText != null || secondaryText != null) Modifier.weight(1.2f) else Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SsplPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    val bg = if (enabled) colors.primaryGradient else SolidColor(colors.border)
    val contentColor = if (enabled) Color.White else colors.muted
    ButtonFrame(modifier.fillMaxWidth(), bg, null, enabled, loading, 52.dp, onClick) {
        if (loading) {
            LoadingDots(color = contentColor, dotSize = 6.dp)
        } else {
            leadingIcon?.invoke()
            Text(text, style = AppTheme.typography.bodyLarge.copy(color = contentColor, fontWeight = FontWeight.SemiBold))
            trailingIcon?.invoke()
        }
    }
}

enum class SsplButtonVariant { Primary, Secondary, Outline, Ghost, Destructive }

@Composable
fun SsplButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: SsplButtonVariant = SsplButtonVariant.Primary,
    icon: ImageVector? = null
) {
    when (variant) {
        SsplButtonVariant.Primary ->
            PrimaryButton(text, onClick, modifier, enabled, loading, icon = icon)
        SsplButtonVariant.Secondary ->
            SecondaryButton(text, onClick, modifier, enabled, loading, icon = icon)
        SsplButtonVariant.Outline ->
            OutlineButton(text, onClick, modifier, enabled, loading, icon)
        SsplButtonVariant.Ghost ->
            TextButtonLink(text, onClick, modifier, enabled)
        SsplButtonVariant.Destructive ->
            DestructiveButton(text, onClick, modifier, enabled, loading, icon)
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = AppTheme.colors
    val contentColor = if (enabled) Color.White else colors.muted
    ButtonFrame(
        modifier.fillMaxWidth(),
        SolidColor(if (enabled) colors.error else colors.border),
        null, enabled, loading, 50.dp, onClick
    ) {
        ButtonContent(text, loading, contentColor, icon, null)
    }
}

@Composable
fun SsplSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    val contentColor = if (enabled) colors.primary else colors.muted
    ButtonFrame(
        modifier.fillMaxWidth(),
        SolidColor(if (enabled) colors.primaryContainer else colors.boxColor),
        null, enabled, loading, 50.dp, onClick
    ) {
        if (loading) {
            LoadingDots(color = contentColor, dotSize = 6.dp)
        } else {
            leadingIcon?.invoke()
            Text(text, style = AppTheme.typography.bodyLarge.copy(color = contentColor, fontWeight = FontWeight.SemiBold))
            trailingIcon?.invoke()
        }
    }
}
