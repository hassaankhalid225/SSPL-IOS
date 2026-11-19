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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(AppTheme.shapes.card)
                .background(AppTheme.colors.surfaceTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(AppTheme.shapes.card)
                .background(AppTheme.colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = AppTheme.colors.error,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = "Try Again",
                onClick = onRetry,
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth(0.5f)
            )
        }
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: androidx.compose.ui.graphics.Shape = AppTheme.shapes.small
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerColors = listOf(
        Color(0xFFE8EEF3),
        Color(0xFFF6F9FC),
        Color(0xFFE8EEF3)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun ShimmerList(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(itemCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppTheme.shapes.card)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, AppTheme.shapes.card)
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f), height = 18.dp)
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 13.dp)
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f), height = 12.dp)
                }
            }
        }
    }
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

