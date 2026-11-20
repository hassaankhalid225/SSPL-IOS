package com.sspl.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sspl.designsystem.theme.AppTheme

/*
 * Skeleton loading kit. The app never shows a circular spinner:
 *  - content loading  → skeletons shaped like the real content
 *  - button / inline  → LoadingDots
 *  - blocking action  → BlockingLoader (dialog with dots + message)
 */

/** Shimmer fill for any shape. Bands sweep diagonally at the same speed everywhere. */
fun Modifier.shimmer(shape: Shape = RoundedCornerShape(10.dp)): Modifier = composed {
    val base = AppTheme.colors.skeletonBase
    val highlight = AppTheme.colors.skeletonHighlight
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    this
        .clip(shape)
        .background(base)
        .drawBehind {
            val w = size.width.coerceAtLeast(1f)
            val x = progress * w
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(x - w * 0.6f, 0f),
                    end = Offset(x, size.height)
                )
            )
        }
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    Box(modifier = modifier.shimmer(shape))
}

@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .shimmer(RoundedCornerShape(50))
    )
}

@Composable
fun SkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size).shimmer(CircleShape))
}

/** Card frame used by every skeleton preset so loading looks like the loaded layout. */
@Composable
private fun SkeletonCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, AppTheme.shapes.card)
            .padding(14.dp)
    ) { content() }
}

/** Row item: thumbnail + three lines + trailing pill. */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier, circleAvatar: Boolean = false) {
    SkeletonCardFrame(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (circleAvatar) SkeletonCircle(48.dp)
            else SkeletonBox(Modifier.size(56.dp), RoundedCornerShape(14.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonLine(widthFraction = 0.75f, height = 14.dp)
                SkeletonLine(widthFraction = 0.5f, height = 11.dp)
                SkeletonLine(widthFraction = 0.35f, height = 10.dp)
            }
            Spacer(Modifier.width(8.dp))
            SkeletonBox(Modifier.width(54.dp).height(22.dp), RoundedCornerShape(50))
        }
    }
}

/** Media card: image + title + meta row. */
@Composable
fun SkeletonMediaCard(modifier: Modifier = Modifier, imageAspect: Float = 16f / 9f) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.surface)
            .border(1.dp, AppTheme.colors.border, AppTheme.shapes.card)
    ) {
        SkeletonBox(
            Modifier.fillMaxWidth().aspectRatio(imageAspect),
            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        )
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine(widthFraction = 0.85f, height = 15.dp)
            SkeletonLine(widthFraction = 0.55f, height = 11.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(Modifier.width(72.dp).height(22.dp), RoundedCornerShape(50))
                SkeletonBox(Modifier.width(56.dp).height(22.dp), RoundedCornerShape(50))
            }
        }
    }
}

enum class SkeletonVariant { LIST, MEDIA, AVATAR_LIST, COMPACT }

/** Vertical skeleton list for any list screen. */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 5,
    variant: SkeletonVariant = SkeletonVariant.LIST,
    contentPadding: Dp = 16.dp
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = contentPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            when (variant) {
                SkeletonVariant.LIST -> SkeletonListItem()
                SkeletonVariant.AVATAR_LIST -> SkeletonListItem(circleAvatar = true)
                SkeletonVariant.MEDIA -> SkeletonMediaCard(Modifier.fillMaxWidth())
                SkeletonVariant.COMPACT -> SkeletonCardFrame {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonLine(widthFraction = 0.7f, height = 14.dp)
                        SkeletonLine(widthFraction = 0.45f, height = 11.dp)
                    }
                }
            }
        }
    }
}

/** Two-column tile grid skeleton (stats / quick actions). */
@Composable
fun SkeletonGrid(modifier: Modifier = Modifier, rows: Int = 2, tileHeight: Dp = 86.dp) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(Modifier.weight(1f).height(tileHeight), AppTheme.shapes.card)
                SkeletonBox(Modifier.weight(1f).height(tileHeight), AppTheme.shapes.card)
            }
        }
    }
}

/** Detail screen skeleton: hero, title, chips, info grid, paragraphs. */
@Composable
fun SkeletonDetail(modifier: Modifier = Modifier, showHero: Boolean = true) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showHero) SkeletonBox(Modifier.fillMaxWidth().aspectRatio(16f / 9f), AppTheme.shapes.card)
        SkeletonLine(widthFraction = 0.85f, height = 20.dp)
        SkeletonLine(widthFraction = 0.5f, height = 13.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { SkeletonBox(Modifier.width(78.dp).height(26.dp), RoundedCornerShape(50)) }
        }
        SkeletonGrid(rows = 2, tileHeight = 64.dp)
        repeat(4) { i -> SkeletonLine(widthFraction = if (i == 3) 0.6f else 1f, height = 11.dp) }
    }
}

/** Three dots that pulse in sequence. Use inside buttons and inline "working…" spots. */
@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
    dotSize: Dp = 7.dp,
    spacing: Dp = 5.dp
) {
    val transition = rememberInfiniteTransition(label = "dots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "dotsPhase"
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val d = ((phase - i + 3f) % 3f)
            val t = if (d < 1f) 1f - kotlin.math.abs(d - 0.5f) * 2f else 0f // 0..1..0 in its slot
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        val s = 0.7f + 0.3f * t
                        scaleX = s; scaleY = s
                        alpha = 0.35f + 0.65f * t
                        translationY = -dotSize.toPx() * 0.35f * t
                    }
                    .background(color, CircleShape)
            )
        }
    }
}

/** Full-screen centred dots for rare cases where no skeleton fits (e.g. WebView boot). */
@Composable
fun CenteredLoading(modifier: Modifier = Modifier, message: String? = null) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingDots(dotSize = 9.dp)
        if (message != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = message,
                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Blocking dialog for submit/pay/upload actions. Not dismissible. */
@Composable
fun BlockingLoader(message: String = "Please wait…") {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .clip(AppTheme.shapes.card)
                .background(AppTheme.colors.surfaceElevated)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDots(dotSize = 9.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                text = message,
                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading),
                textAlign = TextAlign.Center
            )
        }
    }
}
