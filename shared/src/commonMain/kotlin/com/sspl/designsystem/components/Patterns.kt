package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme

/* ==========================================================================================
 * Screen patterns. Screens should be composed from these so the whole app reads as one
 * system: gradient hero cards, icon tiles, stat tiles, list rows, segmented tabs,
 * pull-to-refresh with dots (no spinner) and a single load-state switch.
 * ======================================================================================== */

/** Rounded square tile with a tinted icon. The app's main icon treatment. */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.primary,
    background: Color = AppTheme.colors.primaryContainer,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    corner: Dp = 13.dp,
    /** Draw-on stroke animation on first appearance (tiles, empty states — not long lists). */
    animate: Boolean = false,
    animationDelayMillis: Int = 0
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background)
            // Soft light-to-clear wash + hairline in the icon's own hue: reads crafted, not flat.
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.10f), Color.Transparent)
                )
            )
            .border(1.dp, tint.copy(alpha = 0.14f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (animate) {
            com.sspl.designsystem.icons.AnimatedIcon(
                imageVector = icon, contentDescription = null, tint = tint,
                size = iconSize, delayMillis = animationDelayMillis
            )
        } else {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
        }
    }
}

/**
 * clickable + gentle press-scale (0.96) with spring release.
 * The ripple is clipped to [shape] so the press highlight never spills past rounded corners.
 */
@Composable
fun Modifier.pressClick(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(18.dp),
    onClick: () -> Unit
): Modifier {
    val interaction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (pressed) 0.96f else 1f,
        androidx.compose.animation.core.spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "pressScale"
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick)
}

/**
 * Gradient hero card with soft decorative rings. Use for page intros, highlights,
 * membership card, featured event. Content is white-on-navy.
 */
@Composable
fun GradientHeroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.headerGradient)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        DecorativeRings(Modifier.matchParentSize())
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** Faint concentric rings anchored top-right. Pure decoration for gradient surfaces. */
@Composable
fun DecorativeRings(modifier: Modifier = Modifier, alpha: Float = AppTheme.colors.decorAlpha) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width * 0.92f, size.height * 0.05f)
        for (i in 1..3) {
            drawCircle(
                color = Color.White.copy(alpha = 0.06f * alpha),
                radius = size.minDimension * (0.28f * i),
                center = c,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
            )
        }
        drawCircle(
            color = Color.White.copy(alpha = 0.05f * alpha),
            radius = size.minDimension * 0.22f,
            center = c
        )
    }
}

/** Small label on gradient surfaces (e.g. "UPCOMING", "MEMBER"). */
@Composable
fun GlassPill(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
        Text(
            text = text,
            style = AppTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
        )
    }
}

/** Quick-action tile: icon tile + label (+ optional caption). Use in 2/3/4-column grids. */
@Composable
fun QuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    tint: Color = AppTheme.colors.primary,
    tileBackground: Color = AppTheme.colors.primaryContainer,
    locked: Boolean = false,
    animationDelayMillis: Int = 0
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .pressClick(RoundedCornerShape(18.dp), onClick)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            IconTile(
                icon, tint = if (locked) colors.muted else tint,
                background = if (locked) colors.boxColor else tileBackground, size = 44.dp,
                animate = true, animationDelayMillis = animationDelayMillis
            )
            if (locked) {
                Icon(
                    SsplIcons.Lock, contentDescription = "Sign in required",
                    tint = colors.muted,
                    modifier = Modifier.align(Alignment.TopEnd).size(14.dp)
                        .clip(CircleShape).background(colors.surface).padding(2.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = AppTheme.typography.labelMedium.copy(color = colors.heading, fontWeight = FontWeight.SemiBold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (caption != null) {
            Text(
                text = caption,
                style = AppTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Horizontal chip-like navigation tile: icon + title + chevron (reference-style). */
@Composable
fun NavTile(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color = AppTheme.colors.primary,
    tileBackground: Color = AppTheme.colors.primaryContainer,
    animationDelayMillis: Int = 0
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .pressClick(RoundedCornerShape(18.dp), onClick)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon, tint = tint, background = tileBackground, size = 40.dp, iconSize = 19.dp,
            animate = true, animationDelayMillis = animationDelayMillis
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTheme.typography.titleSmall.copy(fontSize = AppTheme.typography.bodyMedium.fontSize),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(subtitle, style = AppTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(
            SsplIcons.KeyboardArrowRight, contentDescription = null,
            tint = colors.muted, modifier = Modifier.size(18.dp)
        )
    }
}

/** Stat tile: big number + label + optional icon. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = AppTheme.colors.primary,
    onClick: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp)
    ) {
        if (icon != null) {
            IconTile(icon, tint = tint, background = tint.copy(alpha = 0.12f), size = 34.dp, iconSize = 17.dp, corner = 10.dp, animate = true)
            Spacer(Modifier.height(10.dp))
        }
        Text(value, style = AppTheme.typography.number, maxLines = 1)
        Text(label, style = AppTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Stat on a gradient surface (white text). */
@Composable
fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = AppTheme.typography.number.copy(color = Color.White))
        Text(label, style = AppTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.72f)))
    }
}

/**
 * Standard list row card: leading slot, title, subtitle, meta line, trailing slot.
 * Use for registrations, submissions, certificates, members, downloads, etc.
 */
@Composable
fun ListRowCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    meta: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = AppTheme.typography.bodySmall.copy(color = colors.body), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (meta != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(meta, style = AppTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
        if (footer != null) {
            Spacer(Modifier.height(12.dp))
            footer()
        }
    }
}

/** Pill segmented control (Upcoming · Ongoing · Past). Animated indicator, counts optional. */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    counts: List<Int?>? = null
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(colors.boxColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selectedIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (sel) colors.primaryGradient else androidx.compose.ui.graphics.SolidColor(Color.Transparent))
                    .clickable { onSelect(i) }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = AppTheme.typography.labelMedium.copy(
                        color = if (sel) Color.White else colors.body,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    maxLines = 1
                )
                val c = counts?.getOrNull(i)
                if (c != null) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "$c",
                        style = AppTheme.typography.labelSmall.copy(
                            color = if (sel) colors.primary else colors.muted,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) Color.White else colors.surface)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/** Horizontally scrolling filter chips. */
@Composable
fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp
) {
    val colors = AppTheme.colors
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding)
    ) {
        items(options.size) { i ->
            val o = options[i]
            val sel = o == selected
            Text(
                text = o,
                style = AppTheme.typography.labelMedium.copy(
                    color = if (sel) Color.White else colors.body,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (sel) colors.buttonStart else colors.surface)
                    .border(1.dp, if (sel) colors.buttonStart else colors.border, RoundedCornerShape(50))
                    .clickable { onSelect(o) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Pull-to-refresh with the SSPL dots indicator (no circular spinner anywhere in the app).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SsplRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            val fraction = state.distanceFraction.coerceIn(0f, 1f)
            val visible = isRefreshing || fraction > 0f
            val offset by animateDpAsState(if (isRefreshing) 16.dp else (fraction * 24).dp, tween(150), label = "ptr")
            if (visible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = offset)
                        .graphicsLayer { alpha = if (isRefreshing) 1f else fraction }
                        .clip(RoundedCornerShape(50))
                        .background(AppTheme.colors.surfaceElevated)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    LoadingDots(dotSize = 6.dp)
                }
            }
        },
        content = content
    )
}

/** Load state for any screen section. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Error(val message: String) : LoadState<Nothing>
    data class Ready<T>(val data: T) : LoadState<T>
}

/**
 * One switch for loading / error / empty / content with a soft cross-fade.
 * Pass a skeleton that mirrors the real layout.
 */
@Composable
fun <T> LoadStateSwitch(
    state: LoadState<T>,
    modifier: Modifier = Modifier,
    isEmpty: (T) -> Boolean = { (it as? Collection<*>)?.isEmpty() == true },
    skeleton: @Composable () -> Unit = { SkeletonList() },
    empty: @Composable () -> Unit = { EmptyState(title = "Nothing here yet", message = "Check back soon.") },
    onRetry: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
        contentKey = { it::class },
        label = "loadState"
    ) { s ->
        when (s) {
            is LoadState.Loading -> skeleton()
            is LoadState.Error -> ErrorState(message = s.message, onRetry = onRetry)
            is LoadState.Ready -> if (isEmpty(s.data)) empty() else content(s.data)
        }
    }
}

/** Key/value row for detail screens (label above value, icon tile left). */
@Composable
fun FactRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, size = 36.dp, iconSize = 17.dp, corner = 11.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTheme.typography.labelSmall)
            Text(value, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.Medium))
        }
    }
}




