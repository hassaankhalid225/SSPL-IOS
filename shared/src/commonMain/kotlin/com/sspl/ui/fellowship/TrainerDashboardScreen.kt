package com.sspl.ui.fellowship

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.DestructiveButton
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.session.UserSession
import org.koin.compose.koinInject

/**
 * E7: Trainer desk. On the web, trainers are redirected to the trainer portal; the app keeps a
 * native review desk (FellowshipClinicalView isTrainer + FellowshipCompletionView isTrainer) and links to the portal.
 */
@Composable
fun TrainerDashboardScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject(),
    userSession: UserSession = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val vm: TrainerDashboardViewModel = viewModel { TrainerDashboardViewModel(repository, userSession) }
    val state by vm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        when (val c = state.content) {
            is TrainerDeskContent.Loading -> SkeletonDetail()
            is TrainerDeskContent.Error -> ErrorState(title = "Trainer desk didn't load", message = c.message, onRetry = vm::retry)
            is TrainerDeskContent.NotTrainer -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { FellowshipHero("Trainer desk", "For master trainers", "Fellowship dashboard is available after admin enrolls you as a trainer or fellow.", icon = Icons.Default.SupervisorAccount) }
                item {
                    SsplCard {
                        EmptyState(
                            title = "Trainer access only",
                            message = "Accredited centres receive trainer credentials after approval. Register your centre to get started.",
                            icon = Icons.Default.SupervisorAccount,
                            actionText = "Register a training centre",
                            onActionClick = { navController.navigate(Screen.ApplyAsTrainingCentreScreen.route) }
                        )
                    }
                }
            }
            is TrainerDeskContent.Ready -> SsplRefreshBox(isRefreshing = state.isRefreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
                TrainerContent(
                    c, state, vm,
                    onPortal = { urlOpener.openUrl(vm.portalUrl) },
                    onGuidelines = { navController.navigate(Screen.FellowshipGuidelinesScreen.route) },
                    onOpenUrl = { raw -> ImageSources.resolveUrl(raw.replace('\\', '/'))?.let(urlOpener::openUrl) }
                )
            }
        }
        if (state.busy) BlockingLoader("Saving…")
    }
}

private val TRAINER_TABS = listOf("Review cases", "Attendance", "Assessments", "Research", "Fellows")

@Composable
private fun TrainerContent(
    c: TrainerDeskContent.Ready,
    state: TrainerDeskUiState,
    vm: TrainerDashboardViewModel,
    onPortal: () -> Unit,
    onGuidelines: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var sheet by remember { mutableStateOf<String?>(null) }
    var rejectCase by remember { mutableStateOf<Long?>(null) }
    var rejectResearch by remember { mutableStateOf<Long?>(null) }
    val d = c.clinical
    val fellows = d.fellows.ifEmpty { c.dashboardFellows }
    val pendingCases = d.cases.count { it.status == "PENDING" }
    val pendingAtt = d.attendance.count { it.status == "PENDING" }
    val pendingRes = d.research.count { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            GradientHeroCard {
                GlassPill("TRAINER DESK", icon = Icons.Default.SupervisorAccount)
                Spacer(Modifier.height(10.dp))
                Text(c.centerName ?: "Fellowship training", style = AppTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Text(
                    listOfNotNull(c.programName, c.masterTrainer?.let { "Master trainer: $it" }).joinToString(" · ").ifBlank { c.message.orEmpty() },
                    style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.82f))
                )
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HeroStat("${d.stats?.casesPendingReview ?: pendingCases}", "To review")
                    HeroStat("${d.stats?.fellows ?: fellows.size}", "Active fellows")
                    HeroStat("${c.fellowsCount}" + (c.maxFellows?.let { "/$it" } ?: ""), "Assigned")
                    HeroStat("$pendingAtt", "Sessions")
                }
            }
        }
        state.flash?.let { f -> item(key = "flash") { InlineNote(f.text, tone = if (f.isError) NoteTone.Error else NoteTone.Success) } }
        item(key = "portal") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(text = "Trainer portal", onClick = onPortal, icon = Icons.AutoMirrored.Filled.OpenInNew, modifier = Modifier.weight(1f))
                OutlineButton(text = "Guidelines", onClick = onGuidelines, icon = Icons.Default.Description, modifier = Modifier.weight(1f))
            }
        }
        item(key = "tabs") {
            val labels = TRAINER_TABS.mapIndexed { i, l ->
                val n = when (i) { 0 -> pendingCases; 1 -> pendingAtt; 3 -> pendingRes; 4 -> fellows.size; else -> 0 }
                if (n > 0) "$l ($n)" else l
            }
            ChipRow(options = labels, selected = labels[tab], onSelect = { tab = labels.indexOf(it).coerceAtLeast(0) }, horizontalPadding = 0.dp)
        }

        when (tab) {
            0 -> {
                if (d.cases.isEmpty()) item(key = "nc") { SectionEmpty("No logbook cases submitted yet.") }
                items(d.cases, key = { "c${it.id}" }) { cs ->
                    CaseRow(cs, showFellow = true, footer = if (cs.status == "PENDING") ({
                        ReviewActions(onAccept = { vm.acceptCase(cs.id) }, onReject = { rejectCase = cs.id })
                    }) else null)
                }
            }
            1 -> {
                if (d.attendance.isEmpty()) item(key = "na") { SectionEmpty("No attendance records yet.") }
                items(d.attendance, key = { "a${it.id}" }) { a ->
                    AttendanceRow(a, showFellow = true, footer = if (a.status == "PENDING") ({
                        PrimaryButton(text = "Verify", onClick = { vm.verifyAttendance(a.id) }, icon = Icons.Default.Check)
                    }) else null)
                }
            }
            2 -> {
                item(key = "addAs") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryButton(text = "New assessment", onClick = { sheet = "assess" }, icon = Icons.Default.Add, modifier = Modifier.weight(1f), enabled = fellows.isNotEmpty())
                        SecondaryButton(text = "Final evaluation", onClick = { sheet = "final" }, icon = Icons.AutoMirrored.Filled.Send, modifier = Modifier.weight(1f), enabled = fellows.isNotEmpty())
                    }
                }
                if (d.assessments.isEmpty()) item(key = "nas") { SectionEmpty("No assessments recorded yet.") }
                items(d.assessments, key = { "s${it.id}" }) { AssessmentRow(it) }
            }
            3 -> {
                if (d.research.isEmpty()) item(key = "nr") { SectionEmpty("No research submissions yet.") }
                items(d.research, key = { "r${it.id}" }) { r ->
                    ResearchRow(r, onOpenFile = onOpenUrl, footer = if (r.status == "PENDING") ({
                        ReviewActions(onAccept = { vm.acceptResearch(r.id) }, onReject = { rejectResearch = r.id })
                    }) else null)
                }
            }
            else -> {
                if (fellows.isEmpty()) item(key = "nf") { SectionEmpty(c.message ?: "No fellows assigned yet.") }
                items(fellows, key = { "f${it.enrollmentId}" }) { f ->
                    ListRowCard(
                        title = f.name,
                        subtitle = listOfNotNull(f.pmdc?.let { "PMDC $it" }, f.category).joinToString(" · ").ifBlank { null },
                        leading = { IconTile(Icons.Default.Person) },
                        trailing = { StatusBadge("Fellow") }
                    )
                }
            }
        }
    }

    rejectCase?.let { id ->
        FeedbackDialog("Return case for revision", "Tell the fellow what needs to change.", "Needs revision",
            onDismiss = { rejectCase = null }) { fb -> vm.rejectCase(id, fb); rejectCase = null }
    }
    rejectResearch?.let { id ->
        FeedbackDialog("Reject submission", "Optional feedback for the fellow.", "",
            onDismiss = { rejectResearch = null }) { fb -> vm.rejectResearch(id, fb); rejectResearch = null }
    }
    when (sheet) {
        "assess" -> AssessmentSheet(fellows, onDismiss = { sheet = null }) { form -> vm.createAssessment(form) { sheet = null } }
        "final" -> FinalEvaluationSheet(fellows, onDismiss = { sheet = null }) { id, rec, sum -> vm.submitFinalEvaluation(id, rec, sum) { sheet = null } }
    }
}

@Composable
private fun ReviewActions(onAccept: () -> Unit, onReject: () -> Unit) {
    Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DestructiveButton(text = "Reject", onClick = onReject, modifier = Modifier.weight(1f))
        PrimaryButton(text = "Accept", onClick = onAccept, icon = Icons.Default.Check, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FeedbackDialog(title: String, hint: String, initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceElevated,
        title = { Text(title, style = AppTheme.typography.titleLarge.copy(color = AppTheme.colors.heading)) },
        text = { FormField(text, { text = it }, "Feedback", help = hint, multiline = true) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Reject", color = AppTheme.colors.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = AppTheme.colors.muted) } }
    )
}

@Composable
private fun AssessmentSheet(fellows: List<TrainerFellow>, onDismiss: () -> Unit, onSubmit: (Map<String, String>) -> Unit) {
    var fellow by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("Periodic Assessment") }
    var tech by rememberSaveable { mutableStateOf(7) }
    var know by rememberSaveable { mutableStateOf(7) }
    var att by rememberSaveable { mutableStateOf(8) }
    var strengths by rememberSaveable { mutableStateOf("") }
    var areas by rememberSaveable { mutableStateOf("") }
    var recs by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }
    FormSheet("New assessment", "Scores are 1–10.", onDismiss) {
        SelectField("Fellow", fellows.map { it.enrollmentId.toString() to it.name }, fellow, { fellow = it }, required = true,
            placeholder = "Select fellow", error = if (tried && fellow.isBlank()) "Select a fellow." else null)
        FormField(title, { title = it }, "Title")
        Text("Technical", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
        ScaleButtons(tech, { tech = it }, max = 10)
        Text("Knowledge", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
        ScaleButtons(know, { know = it }, max = 10)
        Text("Attitude", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
        ScaleButtons(att, { att = it }, max = 10)
        FormField(strengths, { strengths = it }, "Strengths", multiline = true)
        FormField(areas, { areas = it }, "Areas for improvement", multiline = true)
        FormField(recs, { recs = it }, "Recommendations", multiline = true)
        PrimaryButton(text = "Save assessment", onClick = {
            tried = true
            if (fellow.isBlank()) return@PrimaryButton
            onSubmit(
                mapOf(
                    "enrollment_id" to fellow, "assessment_type" to "PERIODIC", "title" to title,
                    "technical_score" to "$tech", "knowledge_score" to "$know", "attitude_score" to "$att",
                    "strengths" to strengths, "areas_for_improvement" to areas, "recommendations" to recs
                )
            )
        })
    }
}

@Composable
private fun FinalEvaluationSheet(fellows: List<TrainerFellow>, onDismiss: () -> Unit, onSubmit: (String, String, String) -> Unit) {
    var fellow by rememberSaveable { mutableStateOf("") }
    var rec by rememberSaveable { mutableStateOf("COMPLETED") }
    var summary by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }
    FormSheet("Submit final evaluation report", "Sent to the Society admin for certification.", onDismiss) {
        SelectField("Fellow", fellows.map { it.enrollmentId.toString() to it.name }, fellow, { fellow = it }, required = true,
            placeholder = "Select fellow", error = if (tried && fellow.isBlank()) "Select a fellow." else null)
        ChoiceGroup(
            options = listOf("COMPLETED", "CONDITIONAL", "NOT_COMPLETED"), selected = rec, onSelect = { rec = it },
            label = "Recommendation",
            optionLabel = { when (it) { "COMPLETED" -> "Recommend completion"; "CONDITIONAL" -> "Conditional completion"; else -> "Not completed" } }
        )
        FormField(summary, { summary = it }, "Trainer summary & final report", required = true, multiline = true,
            error = if (tried && summary.isBlank()) "Enter the trainer summary." else null)
        PrimaryButton(text = "Submit final evaluation", onClick = {
            tried = true
            if (fellow.isBlank() || summary.isBlank()) return@PrimaryButton
            onSubmit(fellow, rec, summary)
        })
    }
}

