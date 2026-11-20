package com.sspl.ui.elections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.core.models.ElectionAttachment
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.PageIntroHero
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject

private const val ALL = "All"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectionsScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val viewModel: ElectionsViewModel = viewModel { ElectionsViewModel(publicRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val all = state.all
    val filtered = remember(all, state.query, state.phase) {
        val q = state.query.trim()
        all.filter { e ->
            (state.phase == null || e.phase == state.phase) &&
                (q.isEmpty() || e.item.title.contains(q, ignoreCase = true))
        }
    }
    val counts = remember(all) { ElectionPhase.entries.associateWith { p -> all.count { it.phase == p } } }
    var selected by remember { mutableStateOf<ElectionUi?>(null) }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                PageIntroHero(
                    eyebrow = "Governance",
                    eyebrowIcon = Icons.Default.HowToVote,
                    title = "Society elections",
                    summary = "Ongoing and upcoming elections, notices, and official attachments.",
                    stats = if (state.elections is LoadState.Ready) listOf(
                        "${counts[ElectionPhase.ONGOING] ?: 0}" to "Ongoing",
                        "${counts[ElectionPhase.UPCOMING] ?: 0}" to "Upcoming",
                        "${all.size}" to "Total"
                    ) else emptyList(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (state.elections is LoadState.Ready && all.isNotEmpty()) {
                item(key = "search") {
                    SearchBar(
                        query = state.query,
                        onQueryChange = viewModel::onQuery,
                        placeholder = "Search elections",
                        onClear = { viewModel.onQuery("") },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item(key = "chips") {
                    val options = listOf(ALL) + ElectionPhase.entries.map { "${it.label} (${counts[it] ?: 0})" }
                    val selectedLabel = state.phase?.let { "${it.label} (${counts[it] ?: 0})" } ?: ALL
                    ChipRow(
                        options = options,
                        selected = selectedLabel,
                        onSelect = { label ->
                            viewModel.onPhase(ElectionPhase.entries.firstOrNull { label.startsWith(it.label) })
                        }
                    )
                }
            }

            when (val s = state.elections) {
                is LoadState.Loading -> item(key = "skeleton") { SkeletonList(count = 4) }
                is LoadState.Error -> item(key = "error") {
                    ErrorState(title = "Failed to load elections", message = s.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> if (filtered.isEmpty()) {
                    item(key = "empty") {
                        val searching = state.query.isNotBlank() || state.phase != null
                        EmptyState(
                            title = if (searching) "No elections match your search" else "No active elections at the moment",
                            message = "Check back later for updates.",
                            icon = if (searching) Icons.Default.SearchOff else Icons.Default.HowToVote,
                            actionText = if (searching) "Clear filters" else null,
                            onActionClick = if (searching) ({ viewModel.onQuery(""); viewModel.onPhase(null) }) else null
                        )
                    }
                } else {
                    items(filtered, key = { "el_" + it.item.id }) { e ->
                        ElectionCard(e, onClick = { selected = e }, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    selected?.let { e ->
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppTheme.colors.surfaceElevated,
            shape = AppTheme.shapes.sheet
        ) {
            ElectionDetail(e, onOpenFile = { url -> urlOpener.openUrl(url) })
        }
    }
}

private fun phaseVariant(p: ElectionPhase) = when (p) {
    ElectionPhase.ONGOING -> BadgeVariant.SUCCESS
    ElectionPhase.UPCOMING -> BadgeVariant.WARNING
    ElectionPhase.CLOSED -> BadgeVariant.NEUTRAL
}

@Composable
private fun phaseTint(p: ElectionPhase): Pair<Color, Color> = when (p) {
    ElectionPhase.ONGOING -> AppTheme.colors.success to AppTheme.colors.successContainer
    ElectionPhase.UPCOMING -> AppTheme.colors.warning to AppTheme.colors.warningContainer
    ElectionPhase.CLOSED -> AppTheme.colors.muted to AppTheme.colors.boxColor
}

@Composable
private fun ElectionCard(e: ElectionUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val (tint, bg) = phaseTint(e.phase)
    val files = e.item.allFiles
    val dates = DisplayDates.range(e.item.startDate, e.item.endDate)
    Row(
        modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
    ) {
        // Status accent rail.
        Box(Modifier.width(4.dp).heightIn(min = 96.dp).background(tint))
        Column(Modifier.weight(1f).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Default.HowToVote, tint = tint, background = bg)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    StatusBadge(e.phase.label, variant = phaseVariant(e.phase))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        e.item.title.ifBlank { "Election #${e.item.id}" },
                        style = AppTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.muted)
            }
            if (!e.item.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    e.item.description!!,
                    style = AppTheme.typography.bodySmall.copy(color = colors.body),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                if (dates.isNotBlank()) MetaChip(Icons.Default.CalendarMonth, dates)
                MetaChip(Icons.Default.AttachFile, if (files.isEmpty()) "No attachments" else "${files.size} attachment${if (files.size != 1) "s" else ""}")
            }
        }
    }
}

@Composable
private fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppTheme.colors.muted, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = AppTheme.typography.labelSmall)
    }
}

private fun fileIcon(url: String) = when {
    url.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
    Regex("\\.(jpg|jpeg|png|gif|webp|svg)$", RegexOption.IGNORE_CASE).containsMatchIn(url) -> Icons.Default.Image
    else -> Icons.Default.InsertDriveFile
}

private fun fileLabel(f: ElectionAttachment, index: Int): String =
    f.name?.takeIf { it.isNotBlank() }
        ?: "${f.url.substringAfterLast('.', "file").uppercase().take(5)} document ${index + 1}"

@Composable
private fun ElectionDetail(e: ElectionUi, onOpenFile: (String) -> Unit) {
    val colors = AppTheme.colors
    val files = e.item.allFiles
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
    ) {
        StatusBadge(e.phase.label, variant = phaseVariant(e.phase))
        Spacer(Modifier.height(8.dp))
        Text(e.item.title.ifBlank { "Election #${e.item.id}" }, style = AppTheme.typography.headlineSmall.copy(color = colors.heading))
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DisplayDates.medium(e.item.startDate).takeIf { it.isNotBlank() }?.let {
                FactRow(Icons.Default.CalendarMonth, "Starts", it)
            }
            DisplayDates.medium(e.item.endDate).takeIf { it.isNotBlank() }?.let {
                FactRow(Icons.Default.EventAvailable, "Ends", it)
            }
        }
        if (!e.item.description.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(e.item.description!!, style = AppTheme.typography.bodyMedium.copy(color = colors.body))
        }
        Spacer(Modifier.height(18.dp))
        Text("ATTACHMENTS (${files.size})", style = AppTheme.typography.overline)
        Spacer(Modifier.height(8.dp))
        if (files.isEmpty()) {
            Row(
                Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(colors.boxColor).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("No attachments available.", style = AppTheme.typography.bodySmall)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                files.forEachIndexed { i, f ->
                    val url = ImageSources.resolveUrl(f.url) ?: return@forEachIndexed
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.shapes.small)
                            .background(colors.surface)
                            .border(1.dp, colors.border, AppTheme.shapes.small)
                            .clickable { onOpenFile(url) }
                            .heightIn(min = 52.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(fileIcon(url), size = 34.dp, iconSize = 17.dp, corner = 10.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            fileLabel(f, i),
                            style = AppTheme.typography.bodyMedium.copy(color = colors.heading, fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open attachment", tint = colors.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
