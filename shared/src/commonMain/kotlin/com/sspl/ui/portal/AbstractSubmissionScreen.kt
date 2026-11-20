package com.sspl.ui.portal

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme

/**
 * D6: Submit abstract (web `/dashboard/abstracts`): eligible event → title/authors/affiliations →
 * attachment (any media file, ≤100 MB) → multipart POST /abstractSubmission/create.
 */
@Composable
fun AbstractSubmissionScreen(
    navController: NavController,
    viewModel: AbstractSubmissionViewModel = portalViewModel { AbstractSubmissionViewModel(it.get()) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current
    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }
    val pickFile = rememberPortalFilePicker(listOf("any"), viewModel::onFilePicked)
    val f = state.form
    val err = state.errors

    if (state.isSubmitting) BlockingLoader("Submitting your abstract…")

    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PortalHero(
                eyebrow = "Scholarship",
                title = "Submit an abstract",
                summary = "Send your scientific abstract for an eligible conference or monthly meeting.",
                icon = SsplIcons.Send
            )

            StepIndicator(
                currentStep = state.completedStep,
                totalSteps = 4,
                stepLabels = listOf("Choose event", "Abstract details", "Attachment", "Ready to submit")
            )

            state.successMessage?.let {
                PortalNotice(it, NoticeTone.Success, title = "Abstract submitted", onDismiss = viewModel::dismissSuccess)
                TextButtonLink("Track it in My submissions", onClick = { navController.openPortal(Screen.MySubmissionsScreen.route) })
            }
            state.errorMessage?.let { PortalNotice(it, NoticeTone.Error) }

            PortalSection(title = "Event", subtitle = "Where should this abstract be presented?", icon = SsplIcons.Event) {
                when (val ev = state.events) {
                    is LoadState.Loading -> SkeletonBox(Modifier.fillMaxWidth().height(52.dp), AppTheme.shapes.button)
                    is LoadState.Error -> {
                        PortalNotice("Events didn't load: ${ev.message}", NoticeTone.Error)
                        TextButtonLink("Try again", onClick = viewModel::loadEvents)
                    }
                    is LoadState.Ready -> {
                        SheetSelectField(
                            label = "Select event / monthly meeting *",
                            placeholder = if (ev.data.isEmpty()) "No events open for abstracts" else "Select an event or monthly meeting",
                            options = ev.data,
                            selected = state.selectedEvent,
                            optionTitle = { it.title },
                            optionSubtitle = { eventTypeLabel(it.type) },
                            onSelect = { viewModel.selectEvent(it.id) },
                            error = err[AbstractField.EVENT],
                            sheetTitle = "Events accepting abstracts"
                        )
                        if (ev.data.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            PortalNotice("No events are currently accepting abstracts.", NoticeTone.Warning)
                        }
                    }
                }
            }

            PortalSection(title = "Abstract details", icon = SsplIcons.Article) {
                SsplTextField(
                    value = f.title,
                    onValueChange = viewModel::setTitle,
                    label = "Abstract title *",
                    placeholder = "Enter the title of your abstract",
                    error = err[AbstractField.TITLE],
                    singleLine = false, maxLines = 3
                )
                Spacer(Modifier.height(12.dp))
                SsplTextField(
                    value = f.authors,
                    onValueChange = viewModel::setAuthors,
                    label = "Authors *",
                    placeholder = "e.g. John Doe, Jane Smith",
                    error = err[AbstractField.AUTHORS]
                )
                if (err[AbstractField.AUTHORS] == null) {
                    Text("Separate multiple authors with commas.", style = AppTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }
                Spacer(Modifier.height(12.dp))
                SsplTextField(
                    value = f.affiliations,
                    onValueChange = viewModel::setAffiliations,
                    label = "Affiliations *",
                    placeholder = "Enter author affiliations",
                    error = err[AbstractField.AFFILIATIONS],
                    singleLine = false, maxLines = 4
                )
            }

            PortalSection(title = "Attachment", subtitle = "Any media file up to 100 MB", icon = SsplIcons.AttachFile) {
                FilePickerField(
                    label = "Attachment (any media file) *",
                    hint = "Larger files: upload via Google Drive or WeTransfer and add societyofsurgeons1@gmail.com",
                    fileName = f.file?.name,
                    fileSize = f.file?.size,
                    onPick = pickFile,
                    onClear = viewModel::clearFile,
                    error = err[AbstractField.FILE],
                    enabled = !state.isSubmitting
                )
            }

            NavTile(
                icon = SsplIcons.Description,
                title = "My submissions",
                subtitle = "Track review status of your abstracts",
                onClick = { navController.openPortal(Screen.MySubmissionsScreen.route) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        StickyCtaBar(
            primaryText = "Submit abstract",
            onPrimaryClick = viewModel::submit,
            loading = state.isSubmitting,
            enabled = !state.isSubmitting
        )
    }
}
