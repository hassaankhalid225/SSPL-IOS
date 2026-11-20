package com.sspl.ui.portal

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceType
import com.sspl.core.repositories.PortalRegistration
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.theme.AppTheme
import kotlinx.serialization.json.Json
import org.koin.compose.getKoin
import org.koin.core.Koin

/* ==========================================================================================
 * Shared building blocks for the My Portal screens.
 * ======================================================================================== */

/** ViewModel scoped to the nav entry, built from Koin dependencies (portal VMs aren't in ViewModelModules). */
@Composable
inline fun <reified VM : ViewModel> portalViewModel(noinline create: (Koin) -> VM): VM {
    val koin = getKoin()
    return viewModel { create(koin) }
}

/** Page intro: gradient card with eyebrow pill, title, summary and optional hero stats. */
@Composable
fun PortalHero(
    eyebrow: String,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    stats: List<Pair<String, String>> = emptyList(),
    extra: (@Composable ColumnScope.() -> Unit)? = null
) {
    GradientHeroCard(modifier = modifier) {
        GlassPill(eyebrow.uppercase(), icon = icon)
        Spacer(Modifier.height(10.dp))
        Text(title, style = AppTheme.typography.headlineSmall.copy(color = Color.White))
        Spacer(Modifier.height(4.dp))
        Text(summary, style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.78f)))
        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                stats.forEach { (v, l) -> HeroStat(v, l) }
            }
        }
        extra?.invoke(this)
    }
}

/** Inline success / error / warning notice. */
enum class NoticeTone { Success, Error, Warning, Info }

@Composable
fun PortalNotice(
    message: String,
    tone: NoticeTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    val (bg, fg, icon) = when (tone) {
        NoticeTone.Success -> Triple(c.successContainer, c.success, SsplIcons.CheckCircle)
        NoticeTone.Error -> Triple(c.errorContainer, c.error, SsplIcons.ErrorOutline)
        NoticeTone.Warning -> Triple(c.warningContainer, c.warning, SsplIcons.ErrorOutline)
        NoticeTone.Info -> Triple(c.surfaceTint, c.primary, SsplIcons.ErrorOutline)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.small)
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.25f), AppTheme.shapes.small)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            if (title != null) {
                Text(title, style = AppTheme.typography.titleSmall.copy(color = fg))
                Spacer(Modifier.height(2.dp))
            }
            Text(message, style = AppTheme.typography.bodyMedium.copy(color = if (title != null) c.body else fg))
        }
        if (onDismiss != null) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(SsplIcons.Close, contentDescription = "Dismiss", tint = fg, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** A surface card with 20dp radius and hairline border (no shadow) for grouping form sections. */
@Composable
fun PortalSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(c.surface)
            .border(1.dp, c.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconTile(icon, size = 36.dp, iconSize = 18.dp, corner = 11.dp)
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = AppTheme.typography.titleMedium)
                if (subtitle != null) Text(subtitle, style = AppTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

/** Dashed drop-zone style file picker row. */
@Composable
fun FilePickerField(
    label: String,
    hint: String,
    fileName: String?,
    fileSize: Long?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true
) {
    val c = AppTheme.colors
    val stroke = if (error != null) c.error else if (fileName != null) c.primary else c.border
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = AppTheme.typography.labelMedium.copy(
                color = if (error != null) c.error else c.heading, fontWeight = FontWeight.Medium
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(AppTheme.shapes.button)
                .background(if (fileName != null) c.surfaceTint else c.boxColor)
                .drawBehind {
                    drawRoundRect(
                        color = stroke,
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(
                            width = 1.4.dp.toPx(),
                            pathEffect = if (fileName == null) PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) else null
                        )
                    )
                }
                .clickable(enabled = enabled, onClick = onPick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(
                if (fileName != null) SsplIcons.InsertDriveFile else SsplIcons.UploadFile,
                size = 40.dp, iconSize = 20.dp, corner = 12.dp,
                background = c.surface
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    fileName ?: "Choose a file",
                    style = AppTheme.typography.titleSmall.copy(color = if (fileName != null) c.heading else c.primary),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (fileName != null && fileSize != null) formatBytes(fileSize) else hint,
                    style = AppTheme.typography.bodySmall,
                    maxLines = 3
                )
            }
            if (fileName != null) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(enabled = enabled, onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(SsplIcons.Close, contentDescription = "Remove file", tint = c.muted, modifier = Modifier.size(18.dp))
                }
            }
        }
        AnimatedVisibility(visible = error != null) {
            Text(error.orEmpty(), style = AppTheme.typography.labelSmall.copy(color = c.error), modifier = Modifier.padding(start = 4.dp))
        }
    }
}

/** Select field that opens a bottom-sheet list (used for events / abstracts). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SheetSelectField(
    label: String,
    placeholder: String,
    options: List<T>,
    selected: T?,
    optionTitle: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionSubtitle: ((T) -> String?)? = null,
    error: String? = null,
    enabled: Boolean = true,
    sheetTitle: String = label
) {
    val c = AppTheme.colors
    var open by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = AppTheme.typography.labelMedium.copy(
                color = if (error != null) c.error else c.heading, fontWeight = FontWeight.Medium
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(AppTheme.shapes.button)
                .background(c.surface)
                .border(1.dp, if (error != null) c.error else c.border, AppTheme.shapes.button)
                .clickable(enabled = enabled && options.isNotEmpty()) { open = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                selected?.let(optionTitle) ?: placeholder,
                style = AppTheme.typography.bodyLarge.copy(color = if (selected != null) c.heading else c.muted),
                modifier = Modifier.weight(1f),
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Icon(SsplIcons.ExpandMore, contentDescription = "Open options", tint = c.muted)
        }
        AnimatedVisibility(visible = error != null) {
            Text(error.orEmpty(), style = AppTheme.typography.labelSmall.copy(color = c.error), modifier = Modifier.padding(start = 4.dp))
        }
    }
    if (open) {
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surfaceElevated,
            shape = AppTheme.shapes.sheet
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(sheetTitle, style = AppTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                options.forEach { o ->
                    val sel = o == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(AppTheme.shapes.small)
                            .background(if (sel) c.surfaceTint else c.boxColor)
                            .border(1.dp, if (sel) c.primary else Color.Transparent, AppTheme.shapes.small)
                            .clickable { onSelect(o); open = false }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(optionTitle(o), style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            optionSubtitle?.invoke(o)?.let { Text(it, style = AppTheme.typography.bodySmall) }
                        }
                        if (sel) Icon(SsplIcons.CheckCircle, contentDescription = "Selected", tint = c.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

/** Sign-in prompt for guests on member-only pages. */
@Composable
fun PortalGuestGate(onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(
        title = "Sign in to your member portal",
        message = "Your membership, registrations, abstracts and certificates live here. Sign in with your SSPL account to continue.",
        icon = SsplIcons.Lock,
        actionText = "Sign in",
        onActionClick = onSignIn,
        modifier = modifier
    )
}

// ------------------------------------------------------------------------------------------
// Formatting / navigation helpers
// ------------------------------------------------------------------------------------------

fun formatRupees(amount: Double): String {
    if (amount <= 0.0) return "Free"
    val whole = amount.toLong().toString()
    val grouped = whole.reversed().chunked(3).joinToString(",").reversed()
    return "Rs. $grouped"
}

fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024 -> "${(bytes * 10 / (1024 * 1024)) / 10.0} MB"
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

/** Server file path → absolute URL (handles `\` separators like the web does). */
fun resolvePortalFileUrl(path: String?): String? =
    ImageSources.resolveUrl(path?.replace('\\', '/'))

private val EVENT_TYPE_LABELS = mapOf(
    "MONTHLY_MEETING" to "Monthly meeting",
    "MONTHLY_MEETING_MDT" to "Monthly clinical meeting & MDT",
    "CONFERENCE" to "Conference",
    "PROGRAM" to "Scientific programme",
    "WORKSHOP" to "Workshop",
    "SEMINAR_SYMPOSIUM" to "Seminar & symposium",
    "CME_CPD" to "CME / CPD activity",
    "SUMMIT" to "Summit",
    "FELLOWSHIP_TRAINING" to "Fellowship & training course",
    "COURSE_CERTIFICATION" to "Course & certification",
    "RESEARCH_ACADEMIC" to "Research & academic event"
)

fun eventTypeLabel(type: String?): String =
    EVENT_TYPE_LABELS[type?.uppercase()] ?: type?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Event"

/**
 * Opens the existing conference detail (sessions → GPS/selfie attendance → feedback → certificate).
 * That screen reads a Conference JSON under "data" and re-fetches by id.
 */
fun openEventSessions(navController: NavController, reg: PortalRegistration) {
    val id = reg.conferenceId ?: return
    val conf = Conference(
        id = id,
        type = if (reg.type.equals("WORKSHOP", true)) ConferenceType.WORKSHOP else ConferenceType.PROGRAM,
        title = reg.title,
        summary = reg.summary,
        startDate = reg.startDate,
        endDate = reg.endDate,
        createdAt = null,
        updatedAt = null
    )
    navController.currentBackStackEntry?.savedStateHandle?.set("data", Json.encodeToString(conf))
    navController.navigate(Screen.ScientificProgramDetailScreen.route) { launchSingleTop = true }
}

fun NavController.openPortal(route: String) = navigate(route) { launchSingleTop = true }

/** Small round chip used for meta facts inside cards. */
@Composable
fun MetaChip(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(c.boxColor)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = c.muted, modifier = Modifier.size(12.dp))
        Text(text, style = AppTheme.typography.labelSmall.copy(color = c.body), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
