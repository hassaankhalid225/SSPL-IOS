package com.sspl.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.designsystem.theme.AppTheme

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    StateLayout(
        modifier = modifier,
        icon = icon,
        iconTint = AppTheme.colors.primary,
        iconBg = AppTheme.colors.primaryContainer,
        title = title,
        message = message,
        actionText = actionText,
        actionIcon = null,
        onAction = onActionClick
    )
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Couldn't load this",
    onRetry: (() -> Unit)? = null
) {
    StateLayout(
        modifier = modifier,
        icon = Icons.Default.CloudOff,
        iconTint = AppTheme.colors.error,
        iconBg = AppTheme.colors.errorContainer,
        title = title,
        message = message,
        actionText = if (onRetry != null) "Try again" else null,
        actionIcon = Icons.Default.Refresh,
        onAction = onRetry
    )
}

@Composable
private fun StateLayout(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    message: String,
    actionText: String?,
    actionIcon: ImageVector?,
    onAction: (() -> Unit)?
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Soft double ring around the icon.
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(iconBg.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = AppTheme.typography.titleMedium.copy(
                color = AppTheme.colors.heading,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.colors.muted,
                textAlign = TextAlign.Center
            )
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            SecondaryButton(
                text = actionText,
                onClick = onAction,
                icon = actionIcon,
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}

/** Legacy API — now backed by the synced skeleton shimmer. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: androidx.compose.ui.graphics.Shape = AppTheme.shapes.small
) {
    Box(modifier = modifier.height(height).shimmer(shape))
}

/** Legacy API — now renders [SkeletonList]. */
@Composable
fun ShimmerList(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    SkeletonList(modifier = modifier, count = itemCount)
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = AppTheme.typography.titleMedium.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                text = message,
                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    style = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.muted)
                )
            }
        },
        containerColor = AppTheme.colors.surface,
        shape = AppTheme.shapes.card
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestLockSheet(
    onSignIn: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surface,
        shape = AppTheme.shapes.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Sign in to continue",
                style = AppTheme.typography.titleLarge.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "This feature requires an SSPL member account. Please sign in or register to access your portal, submit abstracts, and register for events.",
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.body,
                    textAlign = TextAlign.Center
                )
            )

            PrimaryButton(
                text = "Sign In / Register",
                onClick = {
                    onDismiss()
                    onSignIn()
                }
            )

            SecondaryButton(
                text = "Continue as Guest",
                onClick = onDismiss
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FeedbackBanner(
    message: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isError) AppTheme.colors.errorContainer else AppTheme.colors.surfaceTint
    val textColor = if (isError) AppTheme.colors.error else AppTheme.colors.primary
    val borderColor = if (isError) AppTheme.colors.error.copy(alpha = 0.3f) else AppTheme.colors.primary.copy(alpha = 0.3f)
    val icon = if (isError) Icons.Default.Warning else Icons.Default.Info

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(bgColor)
            .border(1.dp, borderColor, AppTheme.shapes.card)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = AppTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}


