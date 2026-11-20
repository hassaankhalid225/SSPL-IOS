package com.sspl.ui.fellowship

import com.sspl.designsystem.icons.SsplIcons

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatTile
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.session.UserSession
import org.koin.compose.koinInject
import com.sspl.ui.portal.PickedFile
import com.sspl.ui.portal.rememberPortalFilePicker
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close

/**
 * E6: Fellow dashboard — placement, completion progress, certificate and the clinical workspace
 * (logbook, attendance, assessments, research) from FellowshipDashboardView / FellowshipClinicalView.
 */
@Composable
fun FellowDashboardScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject(),
    userSession: UserSession = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val vm: FellowDashboardViewModel = viewModel { FellowDashboardViewModel(repository, userSession) }
    val state by vm.state.collectAsStateWithLifecycle()
    fun open(route: String) = navController.navigate(route) { launchSingleTop = true }

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        when (val c = state.content) {
            is FellowDashboardContent.Loading -> SkeletonDetail()
            is FellowDashboardContent.Error -> ErrorState(title = "Dashboard didn't load", message = c.message, onRetry = vm::retry)
            is FellowDashboardContent.NotEnrolled -> NotEnrolled(c, onApply = {
                FellowshipNavArgs.centerId = null; open(Screen.ApplyAsFellowScreen.route)
            }, onTrainer = { open(Screen.TrainerDashboardScreen.route) }, onHub = { open(Screen.FellowshipHubScreen.route) })
            is FellowDashboardContent.Ready -> SsplRefreshBox(isRefreshing = state.isRefreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
                FellowContent(
                    c, state, vm,
                    onGuidelines = { open(Screen.FellowshipGuidelinesScreen.route) },
                    onFeedback = { open(Screen.FellowshipFeedbackScreen.route) },
                    onVerify = { open(Screen.VerifyCertificateScreen.route) },
                    onOpenUrl = { raw -> ImageSources.resolveUrl(raw.replace('\\', '/'))?.let(urlOpener::openUrl) }
                )
            }
        }
        if (state.busy) BlockingLoader("Saving…")
    }
}

@Composable
private fun NotEnrolled(c: FellowDashboardContent.NotEnrolled, onApply: () -> Unit, onTrainer: () -> Unit, onHub: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            FellowshipHero(eyebrow = "Fellow dashboard", title = "Your fellowship workspace", summary = c.message, icon = SsplIcons.School)
        }
        item {
            SsplCard {
                EmptyState(
                    title = if (c.isTrainer) "You're registered as a trainer" else "No active enrollment",
                    message = if (c.isTrainer) "Review your fellows' logbooks from the trainer desk."
                    else "Once the Society enrolls you on a programme, your logbook, attendance and certificate appear here.",
                    icon = SsplIcons.MenuBook,
                    actionText = if (c.isTrainer) "Open trainer desk" else "Apply for mini fellowship",
                    onActionClick = if (c.isTrainer) onTrainer else onApply
                )
            }
        }
        item { SecondaryButton(text = "Explore fellowship programmes", onClick = onHub) }
    }
}

private val FELLOW_TABS = listOf("Logbook", "Attendance", "Assessments", "Research")

@Composable
private fun FellowContent(
    c: FellowDashboardContent.Ready,
    state: FellowDashboardUiState,
    vm: FellowDashboardViewModel,
    onGuidelines: () -> Unit,
    onFeedback: () -> Unit,
    onVerify: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var sheet by remember { mutableStateOf<String?>(null) }
    val data = c.clinical
    val colors = AppTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            GradientHeroCard {
                GlassPill("FELLOW DASHBOARD", icon = SsplIcons.School)
                Spacer(Modifier.height(10.dp))
                Text(c.programName ?: "Your fellowship placement", style = AppTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                c.centerName?.let { Text(it, style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.82f))) }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HeroStat("${data.stats?.casesAccepted ?: data.cases.count { it.status == "ACCEPTED" }}", "Approved")
                    HeroStat("${data.stats?.casesPending ?: data.cases.count { it.status == "PENDING" }}", "Pending")
                    HeroStat("${(data.stats?.verifiedHours ?: 0.0).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }}h", "Verified")
                    HeroStat(c.category?.removePrefix("Category ")?.let { "Cat $it" } ?: "—", "Category")
                }
            }
        }

        c.certificate?.let { cert ->
            item(key = "cert") { CertificateCard(cert, c.programName, onVerify, onDownload = cert.fileUrl?.let { u -> { onOpenUrl(u) } }) }
        }

        state.flash?.let { f -> item(key = "flash") { InlineNote(f.text, tone = if (f.isError) NoteTone.Error else NoteTone.Success) } }

        c.progress?.let { p -> item(key = "progress") { ProgressCard(p) } }

        item(key = "tiles") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("${data.cases.size}", "Cases logged", Modifier.weight(1f), icon = SsplIcons.MenuBook)
                    StatTile("${data.attendance.size}", "Sessions", Modifier.weight(1f), icon = SsplIcons.CalendarMonth)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("${data.assessments.size}", "Assessments", Modifier.weight(1f), icon = SsplIcons.Assessment)
                    StatTile("${data.research.size}", "Research", Modifier.weight(1f), icon = SsplIcons.Description)
                }
            }
        }

        item(key = "tabs") {
            SegmentedTabs(
                options = FELLOW_TABS, selectedIndex = tab, onSelect = { tab = it },
                counts = listOf(data.cases.size, data.attendance.size, data.assessments.size, data.research.size)
            )
        }

        when (tab) {
            0 -> {
                item(key = "addCase") { PrimaryButton(text = "Log a case", onClick = { sheet = "case" }, icon = SsplIcons.Add) }
                if (data.cases.isEmpty()) item(key = "noCases") { SectionEmpty("No cases logged yet. Log your first procedure for trainer review.") }
                items(data.cases, key = { "case_${it.id}" }) { CaseRow(it, showFellow = false) }
            }
            1 -> {
                item(key = "addAtt") { PrimaryButton(text = "Record attendance", onClick = { sheet = "att" }, icon = SsplIcons.Add) }
                if (data.attendance.isEmpty()) item(key = "noAtt") { SectionEmpty("No attendance recorded yet.") }
                items(data.attendance, key = { "att_${it.id}" }) { AttendanceRow(it, showFellow = false) }
            }
            2 -> {
                if (data.assessments.isEmpty()) item(key = "noAss") { SectionEmpty("No assessments yet. Your trainer records these during training.") }
                items(data.assessments, key = { "ass_${it.id}" }) { AssessmentRow(it) }
            }
            else -> {
                item(key = "addRes") { PrimaryButton(text = "Submit research / publication", onClick = { sheet = "research" }, icon = SsplIcons.Add) }
                if (data.research.isEmpty()) item(key = "noRes") { SectionEmpty("No research or publications submitted yet.") }
                items(data.research, key = { "res_${it.id}" }) { ResearchRow(it, onOpenFile = onOpenUrl) }
            }
        }

        item(key = "placement") {
            SsplCard(Modifier.padding(top = 8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PLACEMENT", style = AppTheme.typography.overline)
                    FactRow(SsplIcons.Business, "Training centre", c.centerName ?: "—")
                    FactRow(SsplIcons.Person, "Master trainer", c.trainerName ?: "—")
                    FactRow(SsplIcons.WorkspacePremium, "Category · status", "${c.category ?: "—"} · ${c.status?.humanize() ?: "—"}")
                    FactRow(SsplIcons.CalendarMonth, "Training started", prettyDate(c.startedAt))
                }
            }
        }

        item(key = "links") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NavTile(SsplIcons.Feedback, "Feedback", onFeedback, Modifier.weight(1f), subtitle = "Part 1 & 2")
                NavTile(SsplIcons.Description, "Guidelines", onGuidelines, Modifier.weight(1f), subtitle = "SOPs")
            }
        }
    }

    when (sheet) {
        "case" -> CaseSheet(c.procedures, onDismiss = { sheet = null }) { form -> vm.createCase(form) { sheet = null } }
        "att" -> AttendanceSheet(onDismiss = { sheet = null }) { form -> vm.createAttendance(form) { sheet = null } }
        "research" -> ResearchSheet(onDismiss = { sheet = null }) { form, file -> vm.submitResearch(form, file) { sheet = null } }
    }
}

@Composable
private fun CertificateCard(cert: CertificateInfo, programName: String?, onVerify: () -> Unit, onDownload: (() -> Unit)?) {
    GradientHeroCard {
        GlassPill("CERTIFICATE ISSUED & VERIFIED", icon = SsplIcons.Verified)
        Spacer(Modifier.height(10.dp))
        Text("Congratulations, Fellow!", style = AppTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Text(
            "Your fellowship training for ${cert.programName ?: programName ?: "the programme"} has been completed and officially certified by SSPL.",
            style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.85f))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            listOfNotNull(cert.serial?.let { "Serial: $it" }, cert.issuedAt?.let { "Issued: ${prettyDate(it)}" }).joinToString("  ·  "),
            style = AppTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.85f))
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onDownload != null) SecondaryButton(text = "Download PDF", onClick = onDownload, icon = SsplIcons.Download, modifier = Modifier.weight(1f))
            SecondaryButton(text = "Verify online", onClick = onVerify, icon = SsplIcons.Verified, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProgressCard(p: CompletionProgress) {
    val colors = AppTheme.colors
    val met = listOf(p.casesMet, p.hoursMet, p.evaluationApproved, p.feedback1, p.feedback2).count { it }
    SsplCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("COMPLETION REQUIREMENTS", style = AppTheme.typography.overline)
                    Text("$met of 5 requirements met", style = AppTheme.typography.titleMedium.copy(color = colors.heading))
                }
                Text("${met * 20}%", style = AppTheme.typography.number.copy(color = colors.primary))
            }
            LinearProgressIndicator(
                progress = { met / 5f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = colors.primary,
                trackColor = colors.boxColor,
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(4.dp))
            RequirementRow("Approved cases", "${p.casesApproved} / ${p.minCases}", p.casesMet)
            RequirementRow("Verified hours", "${fmt(p.hours)} / ${fmt(p.minHours)}", p.hoursMet)
            RequirementRow("Final evaluation approved", null, p.evaluationApproved)
            RequirementRow("Feedback Part 1 (immediate)", null, p.feedback1)
            RequirementRow("Feedback Part 2 (6-month impact)", null, p.feedback2)
        }
    }
}

private fun fmt(d: Double) = if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()

// ---------------------------------------------------------------- forms

@Composable
private fun CaseSheet(procedures: List<String>, onDismiss: () -> Unit, onSubmit: (Map<String, String>) -> Unit) {
    var date by rememberSaveable { mutableStateOf(todayIso()) }
    var role by rememberSaveable { mutableStateOf("ASSISTANT") }
    var ref by rememberSaveable { mutableStateOf("") }
    var dx by rememberSaveable { mutableStateOf("") }
    var proc by rememberSaveable { mutableStateOf(procedures.firstOrNull().orEmpty()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }
    FormSheet("Log a surgical case", "Submitted cases go to your trainer for review.", onDismiss) {
        FormField(date, { date = it }, "Procedure date", required = true, placeholder = "YYYY-MM-DD",
            error = if (tried && date.isBlank()) "Enter the procedure date." else null)
        SelectField("Your role", CASE_ROLES, role, { role = it })
        FormField(ref, { ref = it }, "Patient ref (anonymous)")
        FormField(dx, { dx = it }, "Diagnosis")
        if (procedures.isNotEmpty()) SelectField("Procedure", procedures.map { it to it }, proc, { proc = it })
        FormField(notes, { notes = it }, "Operative notes", multiline = true)
        PrimaryButton(text = "Submit for approval", onClick = {
            tried = true
            if (date.isBlank()) return@PrimaryButton
            onSubmit(
                mapOf(
                    "procedure_date" to date, "patient_ref" to ref, "diagnosis" to dx, "procedure_name" to proc,
                    "role_in_case" to role, "operative_notes" to notes, "complications" to ""
                )
            )
        })
    }
}

@Composable
private fun AttendanceSheet(onDismiss: () -> Unit, onSubmit: (Map<String, String>) -> Unit) {
    var date by rememberSaveable { mutableStateOf(todayIso()) }
    var type by rememberSaveable { mutableStateOf("OPERATING_THEATRE") }
    var hours by rememberSaveable { mutableStateOf("4") }
    var notes by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }
    FormSheet("Record attendance", "Your trainer verifies each session.", onDismiss) {
        FormField(date, { date = it }, "Session date", required = true, placeholder = "YYYY-MM-DD",
            error = if (tried && date.isBlank()) "Enter the session date." else null)
        SelectField("Session type", ATTENDANCE_TYPES, type, { type = it })
        FormField(hours, { hours = it }, "Hours", keyboardType = KeyboardType.Decimal)
        FormField(notes, { notes = it }, "Notes", multiline = true)
        PrimaryButton(text = "Record attendance", onClick = {
            tried = true
            if (date.isBlank()) return@PrimaryButton
            onSubmit(mapOf("session_date" to date, "session_type" to type, "hours" to hours, "notes" to notes))
        })
    }
}

@Composable
private fun ResearchSheet(onDismiss: () -> Unit, onSubmit: (Map<String, String>, PickedFile?) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var authors by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("ARTICLE") }
    var venue by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf(currentYear().toString()) }
    var abstract by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }
    var file by remember { mutableStateOf<PickedFile?>(null) }
    var fileError by remember { mutableStateOf<String?>(null) }
    val pick = rememberPortalFilePicker(mimeTypesForLabels(RESEARCH_TYPES)) { picked ->
        if (picked == null) return@rememberPortalFilePicker
        val ext = picked.name.substringAfterLast('.', "").uppercase().let { if (it == "JPEG") "JPG" else it }
        when {
            picked.bytes == null -> { file = null; fileError = "File is too large to upload." }
            ext !in RESEARCH_EXTENSIONS -> { file = null; fileError = "Use PDF, DOC, DOCX, ZIP or JPG." }
            else -> { file = picked; fileError = null }
        }
    }
    FormSheet("Submit research / publication", "Optionally attach the manuscript, poster or proof.", onDismiss) {
        FormField(title, { title = it }, "Paper / project title", required = true, placeholder = "e.g. Early outcomes of laparoscopic cholecystectomy",
            error = if (tried && title.isBlank()) "Enter the paper / project title." else null)
        FormField(authors, { authors = it }, "Authors", required = true, placeholder = "e.g. Dr. John Doe, Dr. Jane Smith",
            error = if (tried && authors.isBlank()) "Enter the authors." else null)
        SelectField("Publication type", PUBLICATION_TYPES, type, { type = it })
        FormField(venue, { venue = it }, "Journal / conference event", placeholder = "e.g. SURGICON 2027 / Pakistan Journal of Surgery")
        FormField(year, { year = it }, "Publication year", keyboardType = KeyboardType.Number)
        FormField(abstract, { abstract = it }, "Abstract / summary (optional)", multiline = true)
        FieldLabel("Upload manuscript / poster / proof", help = "PDF, DOCX, ZIP or JPG (optional)")
        val f = file
        if (f == null) {
            SecondaryButton(text = "Choose file", onClick = pick, icon = SsplIcons.AttachFile)
        } else {
            Row(
                Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(AppTheme.colors.boxColor).padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(f.name, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading), modifier = Modifier.weight(1f))
                IconButton(onClick = { file = null }) { Icon(SsplIcons.Close, contentDescription = "Remove attachment", tint = AppTheme.colors.muted) }
            }
        }
        FieldError(fileError)
        PrimaryButton(text = "Submit", onClick = {
            tried = true
            if (title.isBlank() || authors.isBlank()) return@PrimaryButton
            onSubmit(
                mapOf(
                    "title" to title, "authors" to authors, "publication_type" to type,
                    "journal_or_event" to venue, "publication_year" to year, "abstract_text" to abstract
                ),
                file
            )
        })
    }
}

private val RESEARCH_TYPES = listOf("PDF", "DOC", "DOCX", "ZIP", "JPG")
private val RESEARCH_EXTENSIONS = setOf("PDF", "DOC", "DOCX", "ZIP", "JPG")
