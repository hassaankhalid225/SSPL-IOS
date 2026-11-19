package com.sspl.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.sspl.core.models.NotificationItem
import com.sspl.core.models.NotificationType
import com.sspl.core.models.SocietyNewsItem
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.BASE_URL
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Society News, 1: App Alerts
    val societyNews by viewModel.societyNews.collectAsState()
    val isLoadingNews by viewModel.isLoadingNews.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "NEWS & ALERTS",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Announcements",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Official notifications, society circulars, and live session alerts.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Segmented Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NotificationTabButton(
                    title = "Society News",
                    badgeCount = societyNews.size,
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                NotificationTabButton(
                    title = "App Alerts",
                    badgeCount = unreadCount,
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }
        }

        // Society News Tab Content
        if (selectedTab == 0) {
            if (isLoadingNews) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppTheme.colors.primary)
                    }
                }
            } else if (societyNews.isEmpty()) {
                item {
                    EmptyNotificationTab(
                        title = "No Society News",
                        description = "Official notices will appear here as soon as they are announced."
                    )
                }
            } else {
                items(societyNews) { item ->
                    NewsItemCard(item = item)
                }
            }
        } else {
            // App Alerts Tab Content
            if (notifications.isNotEmpty() && unreadCount > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text(
                                text = "Mark all as read",
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    EmptyNotificationTab(
                        title = "No Alerts Yet",
                        description = "You're all caught up! Push updates and live session notices will show here."
                    )
                }
            } else {
                items(notifications) { item ->
                    AppAlertCard(
                        item = item,
                        onMarkAsRead = { viewModel.markAsRead(item.id) },
                        onDelete = { viewModel.deleteNotification(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationTabButton(
    title: String,
    badgeCount: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) AppTheme.colors.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = AppTheme.typography.labelMedium,
                color = if (isSelected) Color.White else AppTheme.colors.textMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else AppTheme.colors.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = AppTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsItemCard(item: SocietyNewsItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val img = item.effectiveImage
            if (!img.isNullOrBlank()) {
                val fullUrl = if (img.startsWith("http")) img else "$BASE_URL/${img.removePrefix("/")}"
                AsyncImage(
                    model = fullUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SsplBadge(
                    text = "Official Update",
                    variant = BadgeVariant.PRIMARY
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!item.createdAt.isNullOrBlank()) {
                    Text(
                        text = item.createdAt.take(10),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title ?: "Society Announcement",
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (item.effectiveBody.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.effectiveBody,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.85f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AppAlertCard(
    item: NotificationItem,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) AppTheme.colors.surface else AppTheme.colors.primary.copy(alpha = 0.04f)
        ),
        border = BorderStroke(
            1.dp,
            if (item.isRead) AppTheme.colors.border else AppTheme.colors.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when (item.type) {
                            NotificationType.SESSION_LIVE -> AppTheme.colors.accent.copy(alpha = 0.15f)
                            else -> AppTheme.colors.primary.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.type) {
                        NotificationType.SESSION_LIVE -> Icons.Default.Videocam
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (item.type) {
                        NotificationType.SESSION_LIVE -> AppTheme.colors.accent
                        else -> AppTheme.colors.primary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.message,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = AppTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationTab(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(AppTheme.colors.border.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textMuted
            )
        }
    }
}
