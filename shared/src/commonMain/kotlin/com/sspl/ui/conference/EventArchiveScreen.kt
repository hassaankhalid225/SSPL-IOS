package com.sspl.ui.conference

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.models.ArchiveMaterial
import com.sspl.core.models.EventArchiveRecord
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** C8: Event archive — past events with downloadable materials. */
@Composable
fun EventArchiveScreen(
    navController: NavController,
    viewModel: EventArchiveViewModel = koinViewModel(),
    urlOpener: UrlOpener = koinInject()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SsplRefreshBox(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
            item(key = "hero") {
                GradientHeroCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                    GlassPill("EVENT ARCHIVE", icon = SsplIcons.History)
                    Spacer(Modifier.height(10.dp))
                    Text("Materials from past meetings", style = AppTheme.typography.headlineSmall.copy(color = Color.White))
                    Text(
                        "Presentations, posters and abstract books shared by SSP Lahore.",
                        style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.78f))
                    )
                    if (state.loaded) {
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            HeroStat("${state.archives.size}", "Events")
                            HeroStat("${state.totalMaterials}", "Files")
                        }
                    }
                }
            }
            item(key = "search") {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    placeholder = "Search events or materials",
                    onClear = { viewModel.setSearchQuery("") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            when {
                state.isLoading -> item(key = "sk") { SkeletonList(count = 4) }
                state.errorMessage != null && !state.loaded -> item(key = "err") {
                    ErrorState(message = state.errorMessage ?: "", onRetry = { viewModel.loadArchives() }, modifier = Modifier.padding(top = 24.dp))
                }
                state.visible.isEmpty() -> item(key = "empty") {
                    EmptyState(
                        title = if (state.searchQuery.isNotBlank()) "No matches" else "Archive is empty",
                        message = if (state.searchQuery.isNotBlank()) "Nothing matched \"${state.searchQuery}\". Try another word."
                        else "Materials from past events will appear here once they're published.",
                        icon = if (state.searchQuery.isNotBlank()) SsplIcons.SearchOff else SsplIcons.FolderZip,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                else -> items(state.visible, key = { "arc_${it.id}" }) { archive ->
                    ArchiveCard(
                        archive = archive,
                        onOpen = { m -> ImageSources.resolveUrl(m.fileUrl)?.let(urlOpener::openUrl) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveCard(archive: EventArchiveRecord, onOpen: (ArchiveMaterial) -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    var expanded by rememberSaveable(archive.id) { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .animateContentSize()
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(SsplIcons.History)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(archive.conference?.title ?: "Past event", style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    DisplayDates.range(archive.conference?.startDate, archive.conference?.endDate).ifBlank { "Date not listed" },
                    style = AppTheme.typography.labelSmall
                )
            }
            archive.conference?.type?.let { StatusBadge(it.replace('_', ' ')) }
        }
        archive.notes?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = AppTheme.typography.bodySmall.copy(color = colors.body), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        if (archive.materials.isEmpty()) {
            Text("No downloadable materials for this event.", style = AppTheme.typography.labelMedium)
        } else {
            Row(
                Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable { expanded = !expanded }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${archive.materials.size} materials", style = AppTheme.typography.labelMedium.copy(color = colors.primary), modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) SsplIcons.ExpandLess else SsplIcons.ExpandMore,
                    contentDescription = if (expanded) "Hide materials" else "Show materials",
                    tint = colors.primary
                )
            }
            if (expanded) {
                archive.materials.forEach { m -> MaterialRow(m, onClick = { onOpen(m) }) }
            }
        }
    }
}

@Composable
private fun MaterialRow(material: ArchiveMaterial, onClick: () -> Unit) {
    val icon = when (material.type?.uppercase()) {
        "IMAGE", "POSTER" -> SsplIcons.Image
        "PRESENTATION" -> SsplIcons.PresentToAll
        "ABSTRACT", "ABSTRACT_BOOK" -> SsplIcons.PictureAsPdf
        else -> SsplIcons.Description
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.small)
            .clickable(enabled = !material.fileUrl.isNullOrBlank(), onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, size = 36.dp, iconSize = 17.dp, corner = 11.dp, tint = AppTheme.colors.accent, background = AppTheme.colors.infoContainer)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(material.title ?: "Document", style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text((material.type ?: "File").replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, style = AppTheme.typography.labelSmall)
        }
        Icon(SsplIcons.Download, contentDescription = "Open ${material.title ?: "file"}", tint = AppTheme.colors.primary, modifier = Modifier.size(20.dp))
    }
}
