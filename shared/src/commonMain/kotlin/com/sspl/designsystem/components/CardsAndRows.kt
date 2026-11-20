    package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.ui.draw.shadow
import com.sspl.designsystem.theme.AppTheme

@Composable
fun SsplCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.card
    val base = modifier
        .fillMaxWidth()
        .then(
            if (!colors.isDark) Modifier.shadow(6.dp, shape, ambientColor = colors.shadow.copy(alpha = 0.10f), spotColor = colors.shadow.copy(alpha = 0.10f))
            else Modifier
        )
    val cardColors = CardDefaults.cardColors(containerColor = backgroundColor ?: colors.surface)
    val border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = if (colors.isDark) 1f else 0.7f))
    if (onClick != null) {
        Card(onClick = onClick, modifier = base, shape = shape, colors = cardColors,
            elevation = CardDefaults.cardElevation(0.dp), border = border) {
            Column(modifier = Modifier.padding(14.dp), content = content)
        }
    } else {
        Card(modifier = base, shape = shape, colors = cardColors,
            elevation = CardDefaults.cardElevation(0.dp), border = border) {
            Column(modifier = Modifier.padding(14.dp), content = content)
        }
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
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        if (effectiveActionText != null && effectiveOnActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = effectiveOnActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
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
                    imageVector = SsplIcons.ChevronRight,
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
            .background(AppTheme.colors.primaryContainer),
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
            SsplImage(
                source = effectiveUrl,
                contentDescription = name,
                modifier = Modifier.size(size),
                fallbackIcon = SsplIcons.Person,
                fallbackIconSize = size * 0.5f
            )
        } else {
            val displayInitials = if (initials.isNotBlank()) initials.take(2).uppercase() else "SS"
            Text(
                text = displayInitials,
                style = AppTheme.typography.titleMedium.copy(
                    color = AppTheme.colors.primary,
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

@Composable
fun ModernCapsuleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search events, CME, conferences...",
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(AppTheme.colors.boxColor)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = SsplIcons.Search,
            contentDescription = "Search",
            tint = AppTheme.colors.muted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.muted,
                        fontSize = 13.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.heading,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (onFilterClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, CircleShape)
                    .clickable(onClick = onFilterClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SsplIcons.Tune,
                    contentDescription = "Filter",
                    tint = AppTheme.colors.heading,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ModernQuickCategoryChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) AppTheme.colors.primaryDark else AppTheme.colors.boxColor
    val contentColor = if (isSelected) Color.White else AppTheme.colors.heading
    val strokeColor = if (isSelected) Color.Transparent else AppTheme.colors.border

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, strokeColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = AppTheme.typography.labelSmall.copy(
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ModernMediaCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    badgeText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(AppTheme.colors.primaryLight, AppTheme.colors.primaryDark)
                            )
                        )
                )
            }

            // Dark gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 60f
                        )
                    )
            )

            // Top badge if provided
            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = badgeText,
                        style = AppTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Bottom texts
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ModernSpecsGrid(
    specs: List<Triple<ImageVector, String, String>>, // icon, value, label
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        specs.take(4).forEach { (icon, value, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.boxColor)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AppTheme.colors.primaryDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = AppTheme.typography.labelMedium.copy(
                        color = AppTheme.colors.heading,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = AppTheme.typography.labelSmall.copy(
                        color = AppTheme.colors.muted,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ModernSupportCard(
    title: String = "Need Help?",
    subtitle: String = "Our society secretariat is here to assist with membership, CME certificates, and event registrations.",
    buttonText: String = "Contact Support",
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.boxColor),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleMedium.copy(
                        color = AppTheme.colors.heading,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colors.muted
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(50.dp))
                        .clickable(onClick = onContactClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        style = AppTheme.typography.labelMedium.copy(
                            color = AppTheme.colors.primaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SsplIcons.HeadsetMic,
                    contentDescription = "Support",
                    tint = AppTheme.colors.primaryDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ModernBookingCard(
    title: String,
    dateText: String,
    timeText: String,
    imageUrl: String?,
    statusText: String = "Upcoming",
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onDetailsClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.boxColor)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.primaryDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SsplIcons.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = AppTheme.typography.titleSmall.copy(
                            color = AppTheme.colors.heading,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(AppTheme.colors.warningContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = AppTheme.typography.labelSmall.copy(
                                color = AppTheme.colors.warning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = SsplIcons.CalendarToday,
                        contentDescription = null,
                        tint = AppTheme.colors.muted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = dateText,
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.muted,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = SsplIcons.AccessTime,
                            contentDescription = null,
                            tint = AppTheme.colors.muted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = timeText,
                            style = AppTheme.typography.labelSmall.copy(
                                color = AppTheme.colors.muted,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "View Details",
                            style = AppTheme.typography.labelSmall.copy(
                                color = AppTheme.colors.primaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Icon(
                            imageVector = SsplIcons.ChevronRight,
                            contentDescription = null,
                            tint = AppTheme.colors.primaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

