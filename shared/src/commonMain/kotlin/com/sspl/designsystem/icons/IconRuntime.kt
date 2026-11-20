package com.sspl.designsystem.icons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Stroke weight for the whole set (Lucide default is 2 on a 24 grid; 1.85 reads crisper on phones). */
internal const val ICON_STROKE = 1.85f

/** Raw path data per icon name, used by [AnimatedIcon] for the draw-on effect. */
private val pathRegistry = mutableMapOf<String, List<String>>()

internal fun lucideIcon(name: String, paths: List<String>): ImageVector {
    pathRegistry[name] = paths
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        paths.forEach { d ->
            path(
                fill = null,
                stroke = SolidColor(Color.Black), // tinted by Icon(tint = …)
                strokeLineWidth = ICON_STROKE,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = { addPathNodes(d).forEach { addNode(it) } }
            )
        }
    }.build()
}

private fun androidx.compose.ui.graphics.vector.PathBuilder.addNode(node: androidx.compose.ui.graphics.vector.PathNode) {
    // PathBuilder has no public "add node"; replay node types we get from addPathNodes.
    when (node) {
        is androidx.compose.ui.graphics.vector.PathNode.MoveTo -> moveTo(node.x, node.y)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeMoveTo -> moveToRelative(node.dx, node.dy)
        is androidx.compose.ui.graphics.vector.PathNode.LineTo -> lineTo(node.x, node.y)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeLineTo -> lineToRelative(node.dx, node.dy)
        is androidx.compose.ui.graphics.vector.PathNode.HorizontalTo -> horizontalLineTo(node.x)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeHorizontalTo -> horizontalLineToRelative(node.dx)
        is androidx.compose.ui.graphics.vector.PathNode.VerticalTo -> verticalLineTo(node.y)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeVerticalTo -> verticalLineToRelative(node.dy)
        is androidx.compose.ui.graphics.vector.PathNode.CurveTo -> curveTo(node.x1, node.y1, node.x2, node.y2, node.x3, node.y3)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeCurveTo -> curveToRelative(node.dx1, node.dy1, node.dx2, node.dy2, node.dx3, node.dy3)
        is androidx.compose.ui.graphics.vector.PathNode.ReflectiveCurveTo -> reflectiveCurveTo(node.x1, node.y1, node.x2, node.y2)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeReflectiveCurveTo -> reflectiveCurveToRelative(node.dx1, node.dy1, node.dx2, node.dy2)
        is androidx.compose.ui.graphics.vector.PathNode.QuadTo -> quadTo(node.x1, node.y1, node.x2, node.y2)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeQuadTo -> quadToRelative(node.dx1, node.dy1, node.dx2, node.dy2)
        is androidx.compose.ui.graphics.vector.PathNode.ReflectiveQuadTo -> reflectiveQuadTo(node.x, node.y)
        is androidx.compose.ui.graphics.vector.PathNode.RelativeReflectiveQuadTo -> reflectiveQuadToRelative(node.dx, node.dy)
        is androidx.compose.ui.graphics.vector.PathNode.ArcTo -> arcTo(
            node.horizontalEllipseRadius, node.verticalEllipseRadius, node.theta,
            node.isMoreThanHalf, node.isPositiveArc, node.arcStartX, node.arcStartY
        )
        is androidx.compose.ui.graphics.vector.PathNode.RelativeArcTo -> arcToRelative(
            node.horizontalEllipseRadius, node.verticalEllipseRadius, node.theta,
            node.isMoreThanHalf, node.isPositiveArc, node.arcStartDx, node.arcStartDy
        )
        androidx.compose.ui.graphics.vector.PathNode.Close -> close()
    }
}

/**
 * Icon whose strokes draw themselves on first appearance (≈0.55 s, eased), then sit still.
 * Falls back to a plain [Icon] for non-SSPL vectors. Use on tiles, empty states, heroes —
 * not inside long scrolling lists.
 */
@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    durationMillis: Int = 550,
    delayMillis: Int = 0
) {
    val paths = pathRegistry[imageVector.name]
    if (paths == null) {
        Icon(imageVector, contentDescription, modifier.size(size), tint = tint)
        return
    }
    val progress = remember(imageVector.name) { Animatable(0f) }
    LaunchedEffect(imageVector.name) {
        if (progress.value < 1f) {
            kotlinx.coroutines.delay(delayMillis.toLong())
            progress.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
        }
    }
    val nodes = remember(imageVector.name) { paths.map { addPathNodes(it) } }
    val painter = rememberVectorPainter(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        name = imageVector.name,
        tintColor = Color.Unspecified,
        autoMirror = false
    ) { _, _ ->
        nodes.forEach { n ->
            Path(
                pathData = n,
                stroke = SolidColor(tint),
                strokeLineWidth = ICON_STROKE,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                trimPathEnd = progress.value
            )
        }
    }
    Icon(painter = painter, contentDescription = contentDescription, modifier = modifier.size(size), tint = Color.Unspecified)
}
