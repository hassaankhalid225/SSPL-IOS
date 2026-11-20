package com.sspl.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.designsystem.theme.AppTheme
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
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
    onAvatarClick: (() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.background,
            titleContentColor = AppTheme.colors.heading,
            navigationIconContentColor = AppTheme.colors.heading,
            actionIconContentColor = AppTheme.colors.heading
        ),
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, CircleShape)
                    .clickable(onClick = onNavigationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (canNavigateBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                    contentDescription = if (canNavigateBack) "Back" else "Open navigation menu",
                    tint = AppTheme.colors.heading,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        title = {
            if (isHomeScreen) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.app_logo),
                        contentDescription = "SSPL Logo",
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Text(
                            text = "SSPL",
                            style = AppTheme.typography.titleMedium.copy(
                                color = AppTheme.colors.primaryDark,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = title,
                    style = AppTheme.typography.titleMedium.copy(
                        color = AppTheme.colors.heading,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        },
        actions = {
            if (onNotificationsClick != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.border, CircleShape)
                        .clickable(onClick = onNotificationsClick),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = AppTheme.colors.error,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 99) "99+" else "$unreadNotificationCount",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = AppTheme.colors.heading,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (onAvatarClick != null) {
                AvatarImage(
                    url = avatarUrl,
                    initials = userInitials,
                    size = 36.dp,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(CircleShape)
                        .border(1.dp, AppTheme.colors.border, CircleShape)
                        .clickable(onClick = onAvatarClick)
                )
            }
        }
    )
}

