package com.sspl.ui.conference.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.conference.EventCardUi

/** Large featured card (first item of the list). Fixed aspect ratio = no layout jump. */
@Composable
fun FeaturedEventCard(event: EventCardUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f)) {
            SsplImage(
                source = event.image,
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                fallbackIcon = Icons.Default.Event,
                fallbackIconSize = 44.dp
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                )
            )
            EventDateBlock(event, Modifier.align(Alignment.TopStart).padding(12.dp))
            Row(
                Modifier.align(Alignment.TopEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (event.status == "ongoing") GlassPill("LIVE NOW")
                GlassPill(event.typeLabel.uppercase())
            }
            if (event.relative.isNotEmpty()) {
                GlassPill(
                    event.relative,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    icon = Icons.Default.Schedule
                )
            }
        }
        Column(Modifier.padding(16.dp)) {
            Text(event.title, style = AppTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            EventMetaLine(Icons.Default.CalendarMonth, listOf(event.dateRange, event.timeRange).filter { it.isNotBlank() }.joinToString(" · "))
            Spacer(Modifier.height(4.dp))
            EventMetaLine(Icons.Default.Place, event.venue)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when {
                        event.registrationOpen -> "Registration open"
                        event.status == "past" -> "Event ended"
                        else -> "Registration closed"
                    },
                    style = AppTheme.typography.labelMedium.copy(
                        color = if (event.registrationOpen) colors.success else colors.muted,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.primaryGradient)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (event.registrationOpen) "Register" else "Details",
                        style = AppTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/** Compact media row: thumbnail with date overlay + type, title, date, venue, status badge. */
@Composable
fun EventMediaRow(event: EventCardUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(width = 96.dp, height = 96.dp).clip(RoundedCornerShape(14.dp))) {
            SsplImage(event.image, event.title, Modifier.fillMaxSize(), fallbackIcon = Icons.Default.Event)
            EventDateBlock(event, Modifier.align(Alignment.TopStart).padding(6.dp), compact = true)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.typeLabel.uppercase(),
                    style = AppTheme.typography.overline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (event.status == "ongoing") {
                    Spacer(Modifier.width(6.dp))
                    StatusBadge("Live", variant = BadgeVariant.ERROR)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(event.title, style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            EventMetaLine(Icons.Default.CalendarMonth, event.dateRange)
            Spacer(Modifier.height(2.dp))
            EventMetaLine(Icons.Default.Place, event.venue)
            if (event.registrationOpen) {
                Spacer(Modifier.height(6.dp))
                StatusBadge("Registration open", variant = BadgeVariant.SUCCESS)
            }
        }
    }
}

/** White date block that sits on images. White surface is allowed on image surfaces. */
@Composable
fun EventDateBlock(event: EventCardUi, modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 10.dp else 14.dp))
            .background(Color.White)
            .padding(horizontal = if (compact) 7.dp else 11.dp, vertical = if (compact) 3.dp else 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            event.day,
            style = AppTheme.typography.number.copy(
                color = AppTheme.colors.primaryDark,
                fontSize = if (compact) AppTheme.typography.titleSmall.fontSize else AppTheme.typography.number.fontSize
            )
        )
        Text(
            event.month.uppercase(),
            style = AppTheme.typography.labelSmall.copy(
                color = AppTheme.colors.gradientMid,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun EventMetaLine(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppTheme.colors.muted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = AppTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
