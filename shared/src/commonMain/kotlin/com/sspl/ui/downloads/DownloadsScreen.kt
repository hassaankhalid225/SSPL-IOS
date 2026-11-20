package com.sspl.ui.downloads

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.core.models.DownloadItem
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.LoadMoreFooter
import com.sspl.ui.components.PageIntroHero
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject

@Composable
fun DownloadsScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val viewModel: DownloadsViewModel = viewModel { DownloadsViewModel(publicRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val activeCategory = DOWNLOAD_CATEGORIES.firstOrNull { it.value == state.category } ?: DOWNLOAD_CATEGORIES.first()

    // Infinite scroll: ask for the next page near the end.
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, state.canLoadMore) {
        if (nearEnd && state.canLoadMore && state.loadMoreError == null) viewModel.loadMore()
    }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                PageIntroHero(
                    eyebrow = "Resources",
                    eyebrowIcon = Icons.Default.FolderOpen,
                    title = "Downloads & templates",
                    summary = "Presentation templates, abstracts, posters, logbooks, guidelines, and society documents.",
                    stats = if (state.items is LoadState.Ready) listOf("${state.total}" to "Files", "${DOWNLOAD_CATEGORIES.size - 1}" to "Categories") else emptyList(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item(key = "search") {
                SearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onQuery,
                    placeholder = "Search by title or description",
                    onClear = { viewModel.onQuery("") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item(key = "chips") {
                Column {
                    ChipRow(
                        options = DOWNLOAD_CATEGORIES.map { it.label },
                        selected = activeCategory.label,
                        onSelect = { label -> DOWNLOAD_CATEGORIES.firstOrNull { it.label == label }?.let { viewModel.onCategory(it.value) } }
                    )
                    if (activeCategory.description.isNotBlank()) {
                        Text(
                            activeCategory.description,
                            style = AppTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        )
                    }
                }
            }

            when (val s = state.items) {
                is LoadState.Loading -> item(key = "skeleton") { SkeletonList(count = 5) }
                is LoadState.Error -> item(key = "error") {
                    ErrorState(title = "Downloads didn't load", message = s.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> if (s.data.isEmpty()) {
                    item(key = "empty") {
                        val filtering = state.query.isNotBlank() || state.category.isNotEmpty()
                        EmptyState(
                            title = if (filtering) "No files match" else "No downloads yet",
                            message = if (filtering) "Try a different search or category." else "Templates and documents will appear here once published.",
                            icon = if (filtering) Icons.Default.SearchOff else Icons.Default.FolderOpen,
                            actionText = if (filtering) "Clear filters" else null,
                            onActionClick = if (filtering) ({ viewModel.onQuery(""); viewModel.onCategory("") }) else null
                        )
                    }
                } else {
                    items(s.data, key = { "d_" + it.id }) { file ->
                        DownloadRow(
                            file = file,
                            onPreview = { urlOpener.openUrl(viewModel.fileUrl(file, download = false)) },
                            onDownload = { urlOpener.openUrl(viewModel.fileUrl(file, download = true)) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    if (state.loadingMore || state.loadMoreError != null) {
                        item(key = "more") {
                            LoadMoreFooter(
                                loading = state.loadingMore,
                                error = state.loadMoreError,
                                onRetry = viewModel::loadMore,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long?): String {
    if (bytes == null || bytes < 0) return ""
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes / 1024.0 * 10).toLong() / 10.0} KB"
        else -> "${(bytes / (1024.0 * 1024.0) * 10).toLong() / 10.0} MB"
    }
}

@Composable
private fun formatStyle(format: String?): Triple<ImageVector, Color, Color> {
    val c = AppTheme.colors
    return when (format?.uppercase()) {
        "PDF" -> Triple(Icons.Default.PictureAsPdf, c.error, c.errorContainer)
        "PPT", "PPTX" -> Triple(Icons.Default.Slideshow, c.warning, c.warningContainer)
        "XLS", "XLSX", "CSV" -> Triple(Icons.Default.TableChart, c.success, c.successContainer)
        "DOC", "DOCX" -> Triple(Icons.Default.Description, c.primary, c.primaryContainer)
        "PNG", "JPG", "JPEG", "WEBP" -> Triple(Icons.Default.Image, c.info, c.infoContainer)
        else -> Triple(Icons.Default.InsertDriveFile, c.muted, c.boxColor)
    }
}

@Composable
private fun DownloadRow(file: DownloadItem, onPreview: () -> Unit, onDownload: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val format = file.fileType?.uppercase()?.takeIf { it.length <= 5 }
        ?: file.fileName?.substringAfterLast('.', "")?.uppercase()?.takeIf { it.isNotBlank() }
    val (icon, tint, bg) = formatStyle(format)
    val meta = listOfNotNull(
        format,
        formatBytes(file.fileSize).takeIf { it.isNotBlank() },
        DisplayDates.medium(file.createdAt).takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Column(
        modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconTile(icon, tint = tint, background = bg, size = 46.dp, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                categoryLabel(file.category)?.let {
                    Text(it.uppercase(), style = AppTheme.typography.overline)
                }
                Text(file.title, style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (meta.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(meta, style = AppTheme.typography.labelSmall)
                }
            }
        }
        val desc = file.description
        if (!desc.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                desc,
                style = AppTheme.typography.bodySmall.copy(color = colors.body),
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (desc.length > 90) {
                Text(
                    if (expanded) "Show less" else "Read more",
                    style = AppTheme.typography.labelMedium.copy(color = colors.primary),
                    modifier = Modifier
                        .clip(AppTheme.shapes.small)
                        .clickable { expanded = !expanded }
                        .padding(vertical = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (file.previewable) {
                OutlineButton(text = "Preview", onClick = onPreview, icon = Icons.Default.Visibility, modifier = Modifier.weight(1f))
            }
            SecondaryButton(text = "Download", onClick = onDownload, icon = Icons.Default.Download, modifier = Modifier.weight(1f))
        }
    }
}
