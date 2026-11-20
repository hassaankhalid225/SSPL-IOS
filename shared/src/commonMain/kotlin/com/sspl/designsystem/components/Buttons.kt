package com.sspl.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme

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
    val isSpinning = loading || isLoading
    val bg = backgroundColor ?: if (enabled) AppTheme.colors.primaryDark else AppTheme.colors.border
    val contentColor = if (enabled) Color.White else AppTheme.colors.muted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
            .clip(AppTheme.shapes.button)
            .background(bg)
            .clickable(enabled = enabled && !isSpinning, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSpinning) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
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
    val isSpinning = loading || isLoading
    val strokeColor = if (enabled) AppTheme.colors.primary else AppTheme.colors.border
    val contentColor = if (enabled) AppTheme.colors.primary else AppTheme.colors.muted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(AppTheme.shapes.button)
            .border(1.5.dp, strokeColor, AppTheme.shapes.button)
            .clickable(enabled = enabled && !isSpinning, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSpinning) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
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
            .clickable(enabled = enabled, onClick = onClick)
            .padding(4.dp)
    )
}

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
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.screenHorizontal, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (priceText != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = priceText,
                        style = AppTheme.typography.titleLarge.copy(
                            color = AppTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (priceSubtext != null) {
                        Text(
                            text = priceSubtext,
                            style = AppTheme.typography.labelSmall.copy(
                                color = AppTheme.colors.muted
                            )
                        )
                    }
                }
            }

            if (secondaryText != null && onSecondaryClick != null) {
                SecondaryButton(
                    text = secondaryText,
                    onClick = onSecondaryClick,
                    modifier = if (priceText != null) Modifier.weight(1f) else Modifier.weight(1f)
                )
            }

            PrimaryButton(
                text = primaryText,
                onClick = onPrimaryClick,
                loading = loading,
                enabled = enabled,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
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
    val bg = if (enabled) AppTheme.colors.primaryDark else AppTheme.colors.border
    val contentColor = if (enabled) Color.White else AppTheme.colors.muted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
            .clip(AppTheme.shapes.button)
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                trailingIcon?.invoke()
            }
        }
    }
}

enum class SsplButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive
}

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
        SsplButtonVariant.Primary -> PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            loading = loading,
            icon = icon
        )
        SsplButtonVariant.Secondary, SsplButtonVariant.Outline -> SecondaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            loading = loading,
            icon = icon
        )
        SsplButtonVariant.Ghost -> TextButtonLink(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
        SsplButtonVariant.Destructive -> PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            loading = loading,
            icon = icon
        )
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
    val bg = if (enabled) AppTheme.colors.error else AppTheme.colors.border
    val contentColor = if (enabled) Color.White else AppTheme.colors.muted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(AppTheme.shapes.button)
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
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
    val strokeColor = if (enabled) AppTheme.colors.primary else AppTheme.colors.border
    val contentColor = if (enabled) AppTheme.colors.primary else AppTheme.colors.muted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
            .clip(AppTheme.shapes.button)
            .border(1.dp, strokeColor, AppTheme.shapes.button)
            .background(AppTheme.colors.surface)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppTheme.colors.primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                trailingIcon?.invoke()
            }
        }
    }
}



