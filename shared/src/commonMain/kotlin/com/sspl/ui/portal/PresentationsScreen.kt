package com.sspl.ui.portal

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.PortalPresentation
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject

/**
 * D8: Presentations (web `/dashboard/presentations`): pick an accepted abstract, title, deck file
 * (PPT/PPTX/PDF/DOC/DOCX) → multipart POST /presentations/upload; list of uploaded decks.
 */
@Composable
fun PresentationsScreen(
    navController: NavController,
    urlOpener: UrlOpener = koinInject()
) {
    val shell = LocalShellActions.current
    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }
    // "Upload presentation" on My submissions passes the abstract id (web: ?abstract_id=).
    val preselect = remember { navController.previousBackStackEntry?.savedStateHandle?.get<Long>("abstractId") }
    val viewModel: PresentationsViewModel = portalViewModel { PresentationsViewModel(it.get(), preselect) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pickFile = rememberPortalFilePicker(PRESENTATION_MIME_TYPES, viewModel::onFilePicked)

    if (state.isUploading) BlockingLoader("Uploading your presentation…")

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val ready = (state.data as? LoadState.Ready)?.data
            item(key = "hero") {
                PortalHero(
                    eyebrow = "Scholarship",
                    title = "Upload presentation",
                    summary = "Attach slides or documents (PowerPoint, PDF, Word) for an accepted abstract.",
                    icon = SsplIcons.Slideshow,
                    stats = ready?.let {
                        listOf("${it.accepted.size}" to "Accepted abstracts", "${it.presentations.size}" to "Uploaded")
                    }.orEmpty()
                )
            }

            when (val d = state.data) {
                is LoadState.Loading -> {
                    item(key = "formSk") { SkeletonBox(Modifier.fillMaxWidth().height(260.dp), AppTheme.shapes.card) }
                    item(key = "listSk") { SkeletonList(count = 2, contentPadding = 0.dp) }
                }
                is LoadState.Error -> item(key = "err") {
                    ErrorState(title = "Presentations didn't load", message = d.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> {
                    state.successMessage?.let { msg ->
                        item(key = "ok") { PortalNotice(msg, NoticeTone.Success, onDismiss = viewModel::dismissSuccess) }
                    }
                    if (d.data.accepted.isEmpty()) {
                        item(key = "noAccepted") {
                            PortalNotice(
                                title = "No accepted abstracts",
                                message = "You need an accepted abstract before you can upload a presentation. If your abstract was recently accepted, please try again later.",
                                tone = NoticeTone.Warning
                            )
                        }
                    } else {
                        item(key = "form") { UploadForm(state, viewModel, pickFile) }
                    }

                    item(key = "listHeader") { SectionHeader(title = "My presentations", modifier = Modifier.padding(top = 6.dp)) }
                    state.listError?.let { item(key = "listErr") { PortalNotice("Your uploaded presentations didn't load: $it", NoticeTone.Error) } }
                    if (d.data.presentations.isEmpty() && state.listError == null) {
                        item(key = "listEmpty") {
                            EmptyState(
                                title = "No presentations yet",
                                message = "Decks you upload for accepted abstracts appear here with their review status.",
                                icon = SsplIcons.Slideshow,
                                actionText = if (d.data.accepted.isEmpty()) "View my submissions" else null,
                                onActionClick = if (d.data.accepted.isEmpty()) ({ navController.openPortal(Screen.MySubmissionsScreen.route) }) else null
                            )
                        }
                    } else {
                        items(d.data.presentations, key = { it.item.id }) { p ->
                            PresentationRow(p, onDownload = { resolvePortalFileUrl(p.item.fileUrl)?.let(urlOpener::openUrl) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadForm(state: PresentationsUiState, vm: PresentationsViewModel, pickFile: () -> Unit) {
    val accepted = (state.data as? LoadState.Ready)?.data?.accepted.orEmpty()
    PortalSection(title = "New upload", subtitle = "All fields are required", icon = SsplIcons.Upload) {
        state.errorMessage?.let {
            PortalNotice(it, NoticeTone.Error)
            Spacer(Modifier.height(12.dp))
        }
        SheetSelectField(
            label = "Accepted abstract *",
            placeholder = "Select an abstract…",
            options = accepted,
            selected = state.selectedAbstract,
            optionTitle = { s -> (s.code?.let { "[$it] " } ?: "") + s.item.title },
            optionSubtitle = { it.item.eventTitle },
            onSelect = { vm.selectAbstract(it.item.id) },
            error = state.errors["abstract"],
            sheetTitle = "Your accepted abstracts"
        )
        Spacer(Modifier.height(12.dp))
        SsplTextField(
            value = state.title,
            onValueChange = vm::setTitle,
            label = "Presentation title *",
            placeholder = "Enter the title of your presentation",
            error = state.errors["title"],
            singleLine = false, maxLines = 3
        )
        Spacer(Modifier.height(12.dp))
        FilePickerField(
            label = "Presentation file *",
            hint = "PowerPoint, PDF or Word document (max 100 MB)",
            fileName = state.file?.name,
            fileSize = state.file?.size,
            onPick = pickFile,
            onClear = vm::clearFile,
            error = state.errors["file"],
            enabled = !state.isUploading
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Upload presentation",
            onClick = vm::upload,
            loading = state.isUploading,
            enabled = !state.isUploading,
            icon = SsplIcons.Upload
        )
    }
}

@Composable
private fun PresentationRow(p: PortalPresentation, onDownload: () -> Unit) {
    val c = AppTheme.colors
    val item = p.item
    ListRowCard(
        title = item.title,
        subtitle = item.abstract?.title?.let { "Abstract: $it" },
        meta = listOfNotNull(
            DisplayDates.medium(item.createdAt).takeIf { it.isNotBlank() }?.let { "Uploaded $it" },
            p.conferenceId?.let { "Conference #$it" }
        ).joinToString(" · ").ifBlank { null },
        leading = { IconTile(SsplIcons.Slideshow) },
        trailing = { StatusBadge(item.status ?: "PENDING") },
        footer = if (!p.adminFeedback.isNullOrBlank() || !item.fileUrl.isNullOrBlank()) {
            {
                p.adminFeedback?.takeIf { it.isNotBlank() }?.let { fb ->
                    Column(Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(c.boxColor).padding(10.dp)) {
                        Text("Feedback", style = AppTheme.typography.labelSmall)
                        Text(fb, style = AppTheme.typography.bodySmall.copy(color = c.body, fontStyle = FontStyle.Italic))
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (!item.fileUrl.isNullOrBlank()) {
                    SecondaryButton("Download file", onClick = onDownload, icon = SsplIcons.Download)
                }
            }
        } else null
    )
}
