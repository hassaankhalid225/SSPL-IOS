package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.designsystem.theme.AppTheme

/**
 * Actions the main shell exposes to screens that draw their own header (e.g. Home hero).
 */
data class ShellActions(
    val openDrawer: () -> Unit = {},
    val onNotifications: () -> Unit = {},
    val onAvatar: () -> Unit = {},
    val unreadCount: Int = 0,
    val avatarUrl: String? = null,
    val initials: String = "",
    val displayName: String? = null,
    val isGuest: Boolean = true
)

val LocalShellActions = compositionLocalOf { ShellActions() }

/** 40dp translucent circular icon button for use on gradient surfaces. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    // Gentle bell swing when something is unread: on appear / when the count changes, then every 6 s.
    val swing = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(badgeCount) {
        while (badgeCount > 0) {
            for (angle in listOf(14f, -12f, 9f, -6f, 3f, 0f)) {
                swing.animateTo(angle, androidx.compose.animation.core.tween(90))
            }
            kotlinx.coroutines.delay(6_000)
        }
        swing.snapTo(0f)
    }
    Box(modifier = modifier.size(42.dp)) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, contentDescription = contentDescription, tint = Color.White,
                modifier = Modifier
                    .size(21.dp)
                    .graphicsLayer {
                        rotationZ = swing.value
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.1f)
                    }
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.error)
                    .border(1.5.dp, AppTheme.colors.gradientMid, CircleShape)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else "$badgeCount",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Gradient top bar. Paints behind the status bar and pads its content below it —
 * this is what removes the old blank band at the top of every screen.
 */
@Composable
fun SsplTopBar(
    title: String,
    modifier: Modifier = Modifier,
    isHomeScreen: Boolean = false,
    canNavigateBack: Boolean = false,
    onNavigationClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    avatarUrl: String? = null,
    userInitials: String = "M",
    onAvatarClick: (() -> Unit)? = null,
    subtitle: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.header)
            .background(AppTheme.colors.headerGradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = if (canNavigateBack) SsplIcons.ArrowBack else SsplIcons.Menu,
                contentDescription = if (canNavigateBack) "Back" else "Open menu",
                onClick = onNavigationClick
            )
            Spacer(Modifier.width(12.dp))
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.7f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onNotificationsClick != null) {
                GlassIconButton(
                    icon = SsplIcons.Notifications,
                    contentDescription = "Notifications",
                    onClick = onNotificationsClick,
                    badgeCount = unreadNotificationCount
                )
            }
            if (onAvatarClick != null) {
                Spacer(Modifier.width(10.dp))
                AvatarImage(
                    url = avatarUrl,
                    initials = userInitials,
                    size = 40.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onAvatarClick)
                )
            }
        }
    }
}
