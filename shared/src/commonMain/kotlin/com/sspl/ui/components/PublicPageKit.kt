package com.sspl.ui.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LoadingDots
import com.sspl.designsystem.components.SkeletonListItem
import com.sspl.designsystem.theme.AppTheme

/*
 * Small building blocks shared by the public / society information screens
 * (About, History, Council, Elections, News, FAQs, Guide, Contact, Terms, Downloads,
 * Verify certificate, Members). Composed only from design-system pieces.
 */

/** Page intro: gradient hero with eyebrow pill, title, one-line summary and optional hero stats. */
@Composable
fun PageIntroHero(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    eyebrowIcon: ImageVector? = null,
    stats: List<Pair<String, String>> = emptyList(),
    extra: (@Composable ColumnScope.() -> Unit)? = null
) {
    GradientHeroCard(modifier = modifier) {
        GlassPill(eyebrow.uppercase(), icon = eyebrowIcon)
        Spacer(Modifier.height(10.dp))
        Text(title, style = AppTheme.typography.headlineSmall.copy(color = Color.White))
        if (!summary.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(summary, style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.78f)))
        }
        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                stats.forEach { (value, label) -> HeroStat(value, label) }
            }
        }
        if (extra != null) {
            Spacer(Modifier.height(14.dp))
            extra()
        }
    }
}

/** Card surface used by the info pages (surface + 1dp border, 20dp corners). */
@Composable
fun InfoSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content
    )
}

/**
 * Accordion card: header row (optional leading slot, title, chevron) and animated body.
 * Whole header is a 48dp+ touch target.
 */
@Composable
fun AccordionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(
                1.dp,
                if (expanded) colors.primary.copy(alpha = 0.35f) else colors.border.copy(alpha = 0.7f),
                AppTheme.shapes.card
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = AppTheme.typography.titleSmall)
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = AppTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(30.dp).clip(AppTheme.shapes.chip)
                    .background(if (expanded) colors.primaryContainer else colors.boxColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    SsplIcons.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp).rotate(rotation)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp), content = body)
        }
    }
}

/** Numbered dot used in timelines / step lists. */
@Composable
fun NumberDot(number: String, modifier: Modifier = Modifier, filled: Boolean = false, size: androidx.compose.ui.unit.Dp = 28.dp) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(AppTheme.shapes.chip)
            .then(if (filled) Modifier.background(colors.primaryGradient) else Modifier.background(colors.primaryContainer)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number,
            style = AppTheme.typography.labelSmall.copy(
                color = if (filled) Color.White else colors.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/** Bulleted paragraph (small primary dot + body text). */
@Composable
fun BulletText(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth()) {
        Box(
            Modifier.padding(top = 8.dp).size(6.dp).clip(AppTheme.shapes.chip)
                .background(AppTheme.colors.accent)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
    }
}

/** Section eyebrow + title, used above blocks on info pages. */
@Composable
fun BlockTitle(eyebrow: String?, title: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            IconTile(icon, size = 34.dp, iconSize = 17.dp, corner = 11.dp)
            Spacer(Modifier.width(10.dp))
        }
        Column {
            if (!eyebrow.isNullOrBlank()) Text(eyebrow.uppercase(), style = AppTheme.typography.overline)
            Text(title, style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
        }
    }
}

/**
 * Footer for paged lists: skeleton row + dots while loading more, or an inline retry when
 * the next page failed. Never a spinner.
 */
@Composable
fun LoadMoreFooter(
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    circleAvatar: Boolean = false
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            loading -> {
                SkeletonListItem(Modifier.fillMaxWidth(), circleAvatar = circleAvatar)
                Spacer(Modifier.height(10.dp))
                LoadingDots(dotSize = 6.dp)
            }
            error != null -> Row(
                modifier = Modifier
                    .clip(AppTheme.shapes.chip)
                    .background(AppTheme.colors.errorContainer)
                    .clickable(onClick = onRetry)
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(SsplIcons.Refresh, contentDescription = null, tint = AppTheme.colors.error, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Couldn't load more. Tap to retry",
                    style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.error, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
