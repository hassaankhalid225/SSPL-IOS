package com.sspl.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sspl.designsystem.theme.AppTheme

@Composable
fun SsplCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = AppTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = AppTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = AppTheme.typography.titleLarge.copy(
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    val effectiveActionText = actionText ?: action
    val effectiveOnActionClick = onActionClick ?: onAction

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleMedium.copy(
                color = AppTheme.colors.heading,
                fontWeight = FontWeight.Bold
            )
        )
        if (effectiveActionText != null && effectiveOnActionClick != null) {
            Row(
                modifier = Modifier.clickable(onClick = effectiveOnActionClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = effectiveActionText,
                    style = AppTheme.typography.labelMedium.copy(
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(AppTheme.shapes.small)
                    .background(AppTheme.colors.surfaceTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.muted)
            )
            Text(
                text = value,
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun InfoGrid(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.boxColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.muted)
                        )
                        Text(
                            text = value,
                            style = AppTheme.typography.bodyMedium.copy(
                                color = AppTheme.colors.heading,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
@Composable
fun AvatarImage(
    url: String? = null,
    imageUrl: String? = null,
    base64Image: String? = null,
    name: String? = null,
    initials: String = name?.split(" ")?.filter { it.isNotBlank() }?.map { it.first() }?.take(2)?.joinToString("") ?: "",
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val effectiveUrl = imageUrl ?: url
    val imageBytes = remember(base64Image) {
        if (!base64Image.isNullOrBlank()) {
            try {
                kotlin.io.encoding.Base64.decode(base64Image)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceTint)
            .border(1.dp, AppTheme.colors.border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (imageBytes != null) {
            AsyncImage(
                model = imageBytes,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else if (!effectiveUrl.isNullOrBlank()) {
            AsyncImage(
                model = effectiveUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            val displayInitials = if (initials.isNotBlank()) initials.take(2).uppercase() else "SS"
            Text(
                text = displayInitials,
                style = AppTheme.typography.titleMedium.copy(
                    color = AppTheme.colors.primaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            )
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize()) {
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body),
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis
        )
        if (text.length > 120) {
            Text(
                text = if (expanded) "Show less" else "Read more",
                style = AppTheme.typography.labelMedium.copy(
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp)
            )
        }
    }
}

