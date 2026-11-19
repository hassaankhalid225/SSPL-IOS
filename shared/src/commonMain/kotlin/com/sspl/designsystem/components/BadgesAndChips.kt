package com.sspl.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.designsystem.theme.AppTheme

enum class BadgeVariant {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL
}

fun resolveBadgeVariant(status: String?): BadgeVariant {
    val s = status?.uppercase() ?: return BadgeVariant.NEUTRAL
    return when {
        s.contains("PAID") || s.contains("APPROVED") || s.contains("ACTIVE") ||
                s.contains("COMPLETED") || s.contains("PRESENT") || s.contains("SUCCESS") ||
                s.contains("VERIFIED") -> BadgeVariant.SUCCESS

        s.contains("PENDING") || s.contains("SUBMITTED") || s.contains("IN_REVIEW") ||
                s.contains("UNDER_REVIEW") || s.contains("AWAITING") || s.contains("PROCESSING") -> BadgeVariant.WARNING

        s.contains("FAILED") || s.contains("REJECTED") || s.contains("EXPIRED") ||
                s.contains("CANCELLED") || s.contains("ABSENT") || s.contains("INACTIVE") -> BadgeVariant.ERROR

        s.contains("CONFERENCE") || s.contains("WORKSHOP") || s.contains("MEETING") ||
                s.contains("FELLOW") || s.contains("TRAINER") -> BadgeVariant.INFO

        else -> BadgeVariant.NEUTRAL
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = resolveBadgeVariant(status)
) {
    val colors = AppTheme.colors
    val (bg, textColor) = when (variant) {
        BadgeVariant.SUCCESS -> colors.successContainer to colors.success
        BadgeVariant.WARNING -> colors.warningContainer to colors.warning
        BadgeVariant.ERROR -> colors.errorContainer to colors.error
        BadgeVariant.INFO -> colors.surfaceTint to colors.primary
        BadgeVariant.NEUTRAL -> colors.boxColor to colors.muted
    }

    Box(
        modifier = modifier
            .clip(AppTheme.shapes.chip)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.replace('_', ' ').uppercase(),
            style = AppTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
fun RoleChip(
    role: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(AppTheme.shapes.chip)
            .background(AppTheme.colors.surfaceTint)
            .border(1.dp, AppTheme.colors.border, AppTheme.shapes.chip)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = role.replace('_', ' '),
            style = AppTheme.typography.labelSmall.copy(
                color = AppTheme.colors.primaryDark,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun MembershipBadge(
    label: String = "Member",
    status: String? = null,
    modifier: Modifier = Modifier
) {
    val textLabel = status?.replace('_', ' ')?.uppercase() ?: label
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.chip)
            .background(AppTheme.colors.primaryDark)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.accent)
        )
        Text(
            text = textLabel,
            style = AppTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun StatChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, AppTheme.shapes.card)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = value,
                    style = AppTheme.typography.titleMedium.copy(
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                text = label,
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.muted
                )
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) AppTheme.colors.primary else AppTheme.colors.surface
    val contentColor = if (selected) Color.White else AppTheme.colors.body
    val strokeColor = if (selected) AppTheme.colors.primary else AppTheme.colors.border

    Box(
        modifier = modifier
            .clip(AppTheme.shapes.chip)
            .background(bg)
            .border(1.dp, strokeColor, AppTheme.shapes.chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = AppTheme.typography.labelMedium.copy(
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun FilterChipsRow(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.screenHorizontal, vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, label ->
            FilterChip(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelected(index) }
            )
        }
    }
}

