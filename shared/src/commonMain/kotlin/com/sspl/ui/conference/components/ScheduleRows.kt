package com.sspl.ui.conference.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sspl.core.models.AddOnSet
import com.sspl.core.models.Session
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.FORMAT_TIME_12HOUR
import com.sspl.platform.formatDateTime
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME

/** "09:00 AM" or "" — never throws on odd backend formats. */
fun safeTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching { formatDateTime(dateTime = iso, inFormat = FORMAT_DEFAULT_DATE_TIME, outFormat = FORMAT_TIME_12HOUR) }
        .getOrNull()?.takeIf { it.isNotBlank() }
        ?: iso.substringAfter('T', "").take(5)
}

/**
 * One programme row: time rail on the left, title/summary/venue on the right.
 * Breaks render as a muted inline strip.
 */
@Composable
fun SessionScheduleRow(
    session: Session,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val colors = AppTheme.colors
    val start = safeTime(session.startsAt)
    val end = safeTime(session.endsAt)
    if (session.isBreak) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.small)
                .background(colors.boxColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(SsplIcons.Coffee, contentDescription = "Break", tint = colors.muted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                session.title.ifBlank { "Break" },
                style = AppTheme.typography.labelMedium.copy(color = colors.body),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (start.isNotBlank()) {
                Text(listOf(start, end).filter { it.isNotBlank() }.joinToString(" – "), style = AppTheme.typography.labelSmall)
            }
        }
        return
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(min = 64.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                start.ifBlank { "TBA" },
                style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            if (end.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(end, style = AppTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(6.dp))
            Spacer(
                Modifier
                    .weight(1f)
                    .width(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.primaryContainer)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(session.title, style = AppTheme.typography.titleSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            session.summary?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(3.dp))
                Text(it.trim(), style = AppTheme.typography.bodySmall.copy(color = colors.body), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            val venue = session.venue?.title ?: session.venue?.name ?: session.venue?.address
            if (!venue.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                EventMetaLine(SsplIcons.Place, venue.trim())
            }
            val speakers = session.breakdown.orEmpty().flatMap { b -> b.speakers.map { it.displayName.trim() } }
                .filter { it.isNotBlank() }
                .distinct()
            if (speakers.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Speakers: " + speakers.take(3).joinToString(", ") + if (speakers.size > 3) " +${speakers.size - 3}" else "",
                    style = AppTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (footer != null) {
                Spacer(Modifier.height(10.dp))
                footer()
            }
        }
    }
}

/** Add-on sets (kits, lunch, materials…) as a compact card list. */
@Composable
fun AddOnSetsList(sets: List<AddOnSet>, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sets.forEach { set ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(AppTheme.shapes.card)
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(SsplIcons.Inventory, size = 34.dp, iconSize = 17.dp, corner = 10.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(set.title?.ifBlank { null } ?: "Included", style = AppTheme.typography.titleSmall)
                }
                set.addOns.orEmpty().forEach { a ->
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("•", style = AppTheme.typography.bodyMedium.copy(color = colors.primary))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.title.orEmpty(), style = AppTheme.typography.bodyMedium.copy(color = colors.heading, fontWeight = FontWeight.Medium))
                            a.subtitle?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = AppTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
