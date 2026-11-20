package com.sspl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sspl.core.models.Breakdown
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.conference.components.safeTime

/**
 * Session rundown: time on the left, item title + up to 3 unique speakers on the right.
 * Signature unchanged (ColumnScope extension) so existing callers keep working.
 */
@Composable
fun ColumnScope.SessionBreakDown(
    breakdown: List<Breakdown>,
    isClickEnabled: Boolean = true,
    onClick: (Breakdown) -> Unit = {}
) {
    val colors = AppTheme.colors
    breakdown.forEachIndexed { index, item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.small)
                .clickable(enabled = isClickEnabled) { onClick(item) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.widthIn(min = 64.dp)) {
                Text(
                    safeTime(item.startsAt).ifBlank { "—" },
                    style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.Bold)
                )
                safeTime(item.endsAt).takeIf { it.isNotBlank() }?.let {
                    Text(it, style = AppTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(
                        Modifier.size(6.dp).clip(CircleShape)
                            .background(if (item.isBreak) colors.muted else colors.accent)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        item.title.orEmpty().trim().ifBlank { if (item.isBreak) "Break" else "Talk" },
                        style = AppTheme.typography.bodyMedium.copy(color = colors.heading, fontWeight = FontWeight.Medium)
                    )
                }
                val speakers = item.speakers.map { it.fullName().trim() }.filter { it.isNotBlank() }.distinct().take(3)
                if (speakers.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(speakers.joinToString(" · "), style = AppTheme.typography.bodySmall)
                }
            }
        }
        if (index != breakdown.lastIndex) {
            HorizontalDivider(Modifier.padding(start = 74.dp), color = colors.divider)
        }
    }
}
