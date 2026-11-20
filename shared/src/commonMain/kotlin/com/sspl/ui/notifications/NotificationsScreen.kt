package com.sspl.ui.notifications

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.models.NotificationItem
import com.sspl.core.models.NotificationType
import com.sspl.core.models.SocietyNewsItem
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SkeletonVariant
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PageIntroHero
import com.sspl.utils.DisplayDates
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val alerts by viewModel.notifications.collectAsStateWithLifecycle()
    val unread by viewModel.unreadCount.collectAsStateWithLifecycle()
    // Open on alerts when there is something unread (bell entry point), else announcements.
    var tab by rememberSaveable { mutableStateOf(if (viewModel.unreadCount.value > 0) 1 else 0) }
    val newsCount = (state.news as? LoadState.Ready)?.data?.size

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                PageIntroHero(
                    eyebrow = "News & updates",
                    eyebrowIcon = Icons.Default.Campaign,
                    title = "Society announcements",
                    summary = "Official updates, notices, and announcements from SSP Lahore.",
                    stats = listOfNotNull(
                        newsCount?.let { "$it" to "Announcements" },
                        "$unread" to "Unread alerts"
                    )
                )
            }
            item(key = "tabs") {
                SegmentedTabs(
                    options = listOf("Announcements", "App alerts"),
                    selectedIndex = tab,
                    onSelect = { tab = it },
                    counts = listOf(newsCount, unread.takeIf { it > 0 })
                )
            }

            if (tab == 0) {
                when (val news = state.news) {
                    is LoadState.Loading -> item(key = "skeleton") {
                        SkeletonList(count = 4, variant = SkeletonVariant.LIST, contentPadding = 0.dp)
                    }
                    is LoadState.Error -> item(key = "error") {
                        ErrorState(
                            title = "Announcements didn't load",
                            message = news.message,
                            onRetry = viewModel::retry
                        )
                    }
                    is LoadState.Ready -> if (news.data.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                title = "No announcements yet",
                                message = "Check back soon for society updates.",
                                icon = Icons.Default.Campaign
                            )
                        }
                    } else {
                        items(news.data, key = { "n_" + it.effectiveId.ifBlank { it.hashCode().toString() } }) { item ->
                            NewsCard(item)
                        }
                    }
                }
            } else {
                if (alerts.isNotEmpty() && unread > 0) {
                    item(key = "markAll") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Row(
                                Modifier
                                    .clip(AppTheme.shapes.chip)
                                    .clickable(onClick = viewModel::markAllAsRead)
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = AppTheme.colors.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mark all as read", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
                if (alerts.isEmpty()) {
                    item(key = "alertsEmpty") {
                        EmptyState(
                            title = "You're all caught up",
                            message = "Push updates and live session notices will show here.",
                            icon = Icons.Default.NotificationsNone
                        )
                    }
                } else {
                    items(alerts, key = { "a_" + it.id }) { item ->
                        AlertCard(
                            item = item,
                            onOpen = { if (!item.isRead) viewModel.markAsRead(item.id) },
                            onDelete = { viewModel.deleteNotification(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(item: SocietyNewsItem) {
    var expanded by remember { mutableStateOf(false) }
    val colors = AppTheme.colors
    val body = item.effectiveBody
    val dateText = DisplayDates.medium(item.createdAt).ifBlank { DisplayDates.relative(item.createdAt) }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable { expanded = !expanded }
            .animateContentSize()
    ) {
        if (!item.effectiveImage.isNullOrBlank()) {
            SsplImage(
                source = item.effectiveImage,
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                fallbackIcon = Icons.Default.Campaign
            )
        }
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Default.Campaign, size = 34.dp, iconSize = 17.dp, corner = 11.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title ?: "Society announcement", style = AppTheme.typography.titleSmall)
                    if (dateText.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = colors.muted, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(dateText, style = AppTheme.typography.labelSmall)
                        }
                    }
                }
            }
            if (body.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    body,
                    style = AppTheme.typography.bodyMedium.copy(color = colors.body),
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (body.length > 140) {
                    Text(
                        if (expanded) "Show less" else "Read more",
                        style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun alertTime(ts: Long): String {
    if (ts <= 0) return ""
    val iso = runCatching { Instant.fromEpochMilliseconds(ts).toString() }.getOrNull() ?: return ""
    return DisplayDates.relative(iso).ifBlank { DisplayDates.medium(iso) }
}

@Composable
private fun AlertCard(item: NotificationItem, onOpen: () -> Unit, onDelete: () -> Unit) {
    val colors = AppTheme.colors
    val live = item.type == NotificationType.SESSION_LIVE
    Row(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(if (item.isRead) colors.surface else colors.surfaceTint)
            .border(
                1.dp,
                if (item.isRead) colors.border.copy(alpha = 0.7f) else colors.primary.copy(alpha = 0.3f),
                AppTheme.shapes.card
            )
            .clickable(onClick = onOpen)
            .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box {
            IconTile(
                if (live) Icons.Default.Videocam else Icons.Default.Notifications,
                tint = if (live) colors.accent else colors.primary,
                background = if (live) colors.infoContainer else colors.primaryContainer
            )
            if (!item.isRead) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(10.dp).clip(AppTheme.shapes.chip)
                        .background(colors.error).border(2.dp, colors.surface, AppTheme.shapes.chip)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, style = AppTheme.typography.titleSmall, modifier = Modifier.weight(1f, fill = false))
                if (live) {
                    Spacer(Modifier.width(6.dp))
                    StatusBadge("Live", variant = BadgeVariant.INFO)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(item.message, style = AppTheme.typography.bodySmall.copy(color = colors.body))
            val time = alertTime(item.timestamp)
            if (time.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(time, style = AppTheme.typography.labelSmall)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete alert", tint = colors.muted, modifier = Modifier.size(20.dp))
        }
    }
}
