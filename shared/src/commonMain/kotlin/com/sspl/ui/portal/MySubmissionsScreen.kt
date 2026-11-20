package com.sspl.ui.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.PortalSubmission
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject

/**
 * D7: My submissions (web `/dashboard/submissions`): server-side status filter, pagination,
 * reviewer feedback, upload-presentation for accepted, download attachment.
 */
@Composable
fun MySubmissionsScreen(
    navController: NavController,
    viewModel: MySubmissionsViewModel = portalViewModel { MySubmissionsViewModel(it.get()) },
    urlOpener: UrlOpener = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current
    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }
    val ready = (state.list as? LoadState.Ready)?.data

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                PortalHero(
                    eyebrow = "Scholarship",
                    title = "My submissions",
                    summary = "Review the status of abstracts you have sent for scientific programmes.",
                    icon = Icons.Default.Description,
                    stats = ready?.let { listOf("${it.totalRecords}" to "Total submissions") }.orEmpty()
                )
            }
            item(key = "tabs") {
                SegmentedTabs(
                    options = SUBMISSION_FILTERS.map { it.second },
                    selectedIndex = SUBMISSION_FILTERS.indexOfFirst { it.first == state.filter }.coerceAtLeast(0),
                    onSelect = { viewModel.setFilter(SUBMISSION_FILTERS[it].first) }
                )
            }
            when (val l = state.list) {
                is LoadState.Loading -> item(key = "sk") { SkeletonList(count = 4, contentPadding = 0.dp) }
                is LoadState.Error -> item(key = "err") {
                    ErrorState(title = "Submissions didn't load", message = l.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> if (l.data.items.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = if (state.filter.isBlank()) "No submissions yet" else "Nothing ${SUBMISSION_FILTERS.first { it.first == state.filter }.second.lowercase()}",
                            message = if (state.filter.isBlank()) "You haven't submitted any abstracts yet. Submit one to get started."
                            else "No abstracts match this status.",
                            icon = Icons.Default.Description,
                            actionText = "Submit an abstract",
                            onActionClick = { navController.openPortal(Screen.AbstractSubmissionScreen.route) }
                        )
                    }
                } else {
                    items(l.data.items, key = { it.item.id }) { sub ->
                        SubmissionCard(
                            sub = sub,
                            onUpload = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("abstractId", sub.item.id)
                                navController.openPortal(Screen.PresentationsScreen.route)
                            },
                            onDownload = { resolvePortalFileUrl(sub.item.bestFileUrl)?.let(urlOpener::openUrl) }
                        )
                    }
                    if (l.data.canLoadMore) {
                        item(key = "more") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.loadMoreError?.let { PortalNotice(it, NoticeTone.Error) }
                                OutlineButton(
                                    text = "Load more (page ${l.data.page + 1} of ${l.data.totalPages})",
                                    onClick = viewModel::loadMore,
                                    loading = state.isLoadingMore
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(sub: PortalSubmission, onUpload: () -> Unit, onDownload: () -> Unit) {
    val item = sub.item
    val c = AppTheme.colors
    val accepted = item.status.uppercase() in setOf("ACCEPTED", "APPROVED")
    val hasFile = !item.bestFileUrl.isNullOrBlank()
    ListRowCard(
        title = item.title,
        subtitle = item.eventTitle ?: item.authors,
        meta = listOfNotNull(
            "ID #${sub.displayId}",
            DisplayDates.medium(item.createdAt).takeIf { it.isNotBlank() }?.let { "Submitted $it" }
        ).joinToString(" · "),
        leading = { IconTile(Icons.Default.Description) },
        trailing = { StatusBadge(item.status.ifBlank { "PENDING" }) },
        footer = if (accepted || hasFile || !item.reviewerComments.isNullOrBlank() || (item.eventTitle != null && item.authors != null)) {
            {
                if (item.eventTitle != null && !item.authors.isNullOrBlank()) {
                    Text("Authors: ${item.authors}", style = AppTheme.typography.bodySmall.copy(color = c.body))
                }
                item.reviewerComments?.takeIf { it.isNotBlank() }?.let { fb ->
                    Column(
                        Modifier.fillMaxWidth().padding(top = 8.dp).clip(AppTheme.shapes.small).background(c.boxColor).padding(10.dp)
                    ) {
                        Text("Reviewer feedback", style = AppTheme.typography.labelSmall)
                        Text(fb, style = AppTheme.typography.bodySmall.copy(color = c.body, fontStyle = FontStyle.Italic))
                    }
                }
                if (accepted || hasFile) {
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (accepted) PrimaryButton("Upload presentation", onClick = onUpload, icon = Icons.Default.Upload, modifier = Modifier.weight(1.3f))
                        if (hasFile) SecondaryButton("Download", onClick = onDownload, icon = Icons.Default.Download, modifier = Modifier.weight(1f))
                    }
                }
            }
        } else null
    )
}
