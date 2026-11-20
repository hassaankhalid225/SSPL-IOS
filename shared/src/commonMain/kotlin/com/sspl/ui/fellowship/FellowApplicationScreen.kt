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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LoadingDots
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.sspl.ui.portal.rememberPortalFilePicker

/**
 * E4: Apply as a fellow — config-driven multi-step wizard (ApplicationWizard.jsx).
 */
@Composable
fun FellowApplicationScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject(),
    userSession: UserSession = koinInject()
) {
    val vm: FellowApplicationViewModel = viewModel { FellowApplicationViewModel(repository, userSession) }
    val state by vm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        val receipt = state.receipt
        val program = state.program
        when {
            receipt != null -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    ReceiptCard(
                        eyebrow = "Application status",
                        title = "Application submitted successfully",
                        rows = listOf("Application ID" to receipt.id, "Program" to receipt.program, "Status" to receipt.status),
                        note = "You will receive an email when your application status changes.",
                        primaryText = "Back to fellowship",
                        onPrimary = { navController.popBackStack() },
                        secondaryText = "Programme guidelines",
                        onSecondary = { navController.navigate(Screen.FellowshipGuidelinesScreen.route) }
                    )
                }
            }
            program != null -> Wizard(vm, state, program)
            else -> when (val p = state.programs) {
                is LoadState.Loading -> SkeletonDetail(showHero = false)
                is LoadState.Error -> ErrorState(title = "Couldn't load the application", message = p.message, onRetry = vm::loadPrograms)
                is LoadState.Ready -> if (p.data.isEmpty()) {
                    EmptyState(
                        title = "Applications are not open right now",
                        message = "No fellowship programme is currently accepting applications. Check the guidelines to prepare for the next intake.",
                        icon = SsplIcons.School,
                        actionText = "Read guidelines",
                        onActionClick = { navController.navigate(Screen.FellowshipGuidelinesScreen.route) }
                    )
                } else {
                    ProgramChooser(p.data, onPick = vm::selectProgram, title = "Choose a fellowship",
                        subtitle = "More than one fellowship is open. Apply for the program you want.")
                }
            }
        }
        if (state.submitting) BlockingLoader("Submitting application…")
    }
}

@Composable
internal fun ProgramChooser(programs: List<Program>, onPick: (Program) -> Unit, title: String, subtitle: String) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FellowshipHero(eyebrow = "Fellowship", title = title, summary = subtitle, icon = SsplIcons.School) }
        items(programs, key = { it.id ?: it.name.hashCode().toLong() }) { p ->
            ListRowCard(
                title = p.name,
                subtitle = p.shortName.ifBlank { null },
                meta = p.description.ifBlank { null },
                leading = { IconTile(SsplIcons.School) },
                trailing = { Icon(SsplIcons.ArrowForward, contentDescription = null, tint = AppTheme.colors.primary) },
                onClick = { onPick(p) }
            )
        }
    }
}

@Composable
private fun Wizard(vm: FellowApplicationViewModel, state: FellowApplicationUiState, program: Program) {
    val listState = rememberLazyListState()
    val errors = if (state.showErrors) vm.errorsFor(state, state.step) else emptyMap()
    val steps = state.steps
    var pendingDoc by remember { mutableStateOf<DocumentDef?>(null) }
    // One picker filtered to every type this programme accepts; each document is re-validated in the VM.
    val pickFile = rememberPortalFilePicker(mimeTypesForLabels(program.documents.flatMap { it.fileTypes })) { picked ->
        val doc = pendingDoc ?: return@rememberPortalFilePicker
        if (picked != null) vm.uploadDocument(doc, picked)
    }

    LaunchedEffect(state.stepIndex) { listState.scrollToItem(0) }
    LaunchedEffect(state.showErrors, state.stepIndex) { if (state.showErrors) listState.animateScrollToItem(0) }

    val programsCount = (state.programs as? LoadState.Ready)?.data?.size ?: 1

    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "head") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (programsCount > 1) TextButtonLink(text = "All fellowships", onClick = vm::clearProgram)
                    Text("APPLY AS A FELLOW", style = AppTheme.typography.overline)
                    Text(program.name, style = AppTheme.typography.headlineSmall.copy(color = AppTheme.colors.heading))
                    if (program.shortName.isNotBlank()) Text(program.shortName, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary))
                }
            }
            item(key = "steps") {
                SsplCard {
                    StepIndicator(currentStep = state.stepIndex + 1, totalSteps = steps.size, stepLabels = steps.map { it.second })
                }
            }
            if (errors.isNotEmpty()) {
                item(key = "errs") { InlineNote("Please fix the highlighted fields to continue.", tone = NoteTone.Error) }
            }
            state.submitError?.let { msg -> item(key = "submitErr") { InlineNote(msg, tone = NoteTone.Error, title = "Submission failed") } }

            item(key = "step_${state.step}") {
                when (state.step) {
                    "personal" -> FormSection("Personal information", subtitle = "These details identify you on the Society record.") {
                        program.personalFields.forEach { f ->
                            FormField(
                                value = state.personal[f.key].orEmpty(),
                                onValueChange = { vm.setPersonal(f.key, it) },
                                label = f.label, required = f.required, help = f.help, error = errors[f.key],
                                keyboardType = when (f.key) {
                                    "email" -> KeyboardType.Email
                                    "mobile", "whatsapp" -> KeyboardType.Phone
                                    "cnic" -> KeyboardType.Number
                                    else -> KeyboardType.Text
                                },
                                multiline = f.key == "postal_address"
                            )
                        }
                    }
                    "training" -> FormSection("Primary training", subtitle = "Where you trained, and how you practise now.") {
                        program.trainingFields.forEach { f ->
                            FormField(
                                value = state.training[f.key].orEmpty(),
                                onValueChange = { vm.setTraining(f.key, it) },
                                label = f.label, required = f.required, help = f.help, error = errors[f.key],
                                keyboardType = if (f.key == "year_completion") KeyboardType.Number else KeyboardType.Text,
                                multiline = f.key == "current_practice"
                            )
                        }
                    }
                    "experience" -> FormSection(program.experienceTitle, subtitle = "Choose the category that best describes your current experience.") {
                        ChoiceGroup(
                            options = program.categories.map { it.code },
                            selected = state.categoryCode,
                            onSelect = vm::setCategory,
                            error = errors["category"],
                            optionLabel = { code -> program.categories.first { it.code == code }.let { c -> if (c.shortLabel.isNotBlank()) "${c.name} — ${c.shortLabel}" else c.name } }
                        )
                        state.category?.questions?.forEach { q -> QuestionInput(q, state.answers[q.id].orEmpty(), { vm.setAnswer(q.id, it) }, errors[q.id]) }
                    }
                    "preferences" -> PreferencesStep(vm, state, errors)
                    "documents" -> DocumentsStep(program, state, errors, onAttach = { pendingDoc = it; pickFile() }, onRemove = vm::removeDocument)
                    "declaration" -> DeclarationStep(vm, state, program, errors)
                }
            }
        }
        WizardBar(
            isFirst = state.stepIndex == 0,
            isLast = state.stepIndex == steps.lastIndex,
            onBack = vm::back,
            onNext = { vm.next() },
            loading = state.submitting,
            enabled = state.files.values.none { it.uploading }
        )
    }
}

@Composable
private fun QuestionInput(q: QuestionDef, value: String, onChange: (String) -> Unit, error: String?) {
    when (q.type) {
        "radio" -> ChoiceGroup(q.options, value, onChange, label = q.label, required = q.required, help = q.help, error = error)
        "dropdown" -> SelectField(q.label, q.options.map { it to it }, value, onChange, required = q.required, help = q.help, error = error)
        else -> FormField(
            value = value, onValueChange = onChange, label = q.label, required = q.required, help = q.help, error = error,
            keyboardType = if (q.type == "number") KeyboardType.Number else KeyboardType.Text,
            multiline = q.type == "textarea"
        )
    }
}

@Composable
private fun PreferencesStep(vm: FellowApplicationViewModel, state: FellowApplicationUiState, errors: Map<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormSection(
            "Training centre preferences",
            eyebrow = "Section D",
            subtitle = "Rank up to three accredited centres. Final placement depends on availability."
        ) {
            if (state.centers.isEmpty()) {
                InlineNote("No accredited centres are listed for this programme yet. The Society will assign a centre after review.")
            } else {
                val all = state.centers.map { it.id.toString() to it.label }
                SelectField("Primary centre (1st choice)", all, state.center1, { vm.setCenter(1, it) }, required = true, error = errors["center1"],
                    placeholder = "Select primary centre", help = "Choose your first-preference training hospital.")
                SelectField("2nd choice (optional)", all.filter { it.first != state.center1 }, state.center2, { vm.setCenter(2, it) },
                    placeholder = "No second choice", allowClear = true)
                SelectField("3rd choice (optional)", all.filter { it.first != state.center1 && it.first != state.center2 }, state.center3, { vm.setCenter(3, it) },
                    placeholder = "No third choice", allowClear = true)
            }
        }
        if (state.visiblePreferences.isNotEmpty()) {
            FormSection("Additional preferences") {
                state.visiblePreferences.forEach { f ->
                    val v = state.preferences[f.id]
                    when (f.type) {
                        "radio" -> if (f.options.all { it.length <= 14 }) {
                            PillChoice(f.options, v as? String, { vm.setPreference(f.id, it) }, label = f.label, required = f.required, error = errors[f.id])
                            if (f.help != null && errors[f.id] == null) Text(f.help, style = AppTheme.typography.bodySmall)
                        } else ChoiceGroup(f.options, v as? String, { vm.setPreference(f.id, it) }, label = f.label, required = f.required, help = f.help, error = errors[f.id])
                        "dropdown" -> SelectField(f.label, f.options.map { it to it }, v as? String, { vm.setPreference(f.id, it) }, required = f.required, help = f.help, error = errors[f.id])
                        "checkbox" -> {
                            @Suppress("UNCHECKED_CAST")
                            CheckGroup(f.options, (v as? List<String>).orEmpty(), { vm.togglePreference(f.id, it) }, label = f.label, required = f.required, help = f.help, error = errors[f.id])
                        }
                        else -> FormField(
                            value = v as? String ?: "", onValueChange = { vm.setPreference(f.id, it) }, label = f.label, required = f.required,
                            help = f.help, error = errors[f.id], multiline = f.type == "textarea",
                            keyboardType = if (f.type == "number") KeyboardType.Number else KeyboardType.Text
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentsStep(
    program: Program,
    state: FellowApplicationUiState,
    errors: Map<String, String>,
    onAttach: (DocumentDef) -> Unit,
    onRemove: (String) -> Unit
) {
    val colors = AppTheme.colors
    FormSection("Documents", subtitle = "Attach each document in an accepted format. Required documents must finish uploading before you continue.") {
        program.documents.forEach { d ->
            val f = state.files[d.id]
            val err = errors[d.id]?.takeIf { f?.error == null }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(SsplIcons.Description, size = 38.dp, iconSize = 18.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(labelOf(d.name, d.required), style = AppTheme.typography.titleSmall.copy(color = colors.heading))
                        Text("${d.fileTypes.joinToString(", ")} · up to ${d.maxSizeMb.let { if (it % 1.0 == 0.0) it.toInt() else it }} MB", style = AppTheme.typography.bodySmall)
                    }
                }
                when {
                    f == null -> SecondaryButton(text = "Choose file", onClick = { onAttach(d) }, icon = SsplIcons.AttachFile)
                    else -> Row(
                        Modifier.fillMaxWidth().background(colors.boxColor, AppTheme.shapes.small).padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            f.uploading -> LoadingDots()
                            f.error != null -> Icon(SsplIcons.ErrorOutline, contentDescription = "Upload failed", tint = colors.error, modifier = Modifier.size(18.dp))
                            else -> Icon(SsplIcons.CheckCircle, contentDescription = "Uploaded", tint = colors.success, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                            Text(f.fileName, style = AppTheme.typography.bodyMedium.copy(color = colors.heading))
                            Text(
                                when {
                                    f.uploading -> "Uploading…"
                                    f.error != null -> f.error
                                    else -> "Uploaded to secure storage"
                                },
                                style = AppTheme.typography.labelMedium.copy(
                                    color = when { f.error != null -> colors.error; f.url != null -> colors.success; else -> colors.muted },
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        if (!f.uploading) {
                            IconButton(onClick = { onRemove(d.id); if (f.error != null) onAttach(d) }) {
                                Icon(SsplIcons.Close, contentDescription = "Remove ${d.name}", tint = colors.muted)
                            }
                        }
                    }
                }
                FieldError(err)
            }
        }
    }
}

@Composable
private fun DeclarationStep(vm: FellowApplicationViewModel, state: FellowApplicationUiState, program: Program, errors: Map<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormSection("Declaration and submission") {
            Text(program.declaration, style = AppTheme.typography.bodyMedium)
            OptionRow(text = "I agree to this declaration.", checked = state.agreed, radio = false) { vm.setAgreed(!state.agreed) }
            FieldError(errors["agreed"])
            FormField(state.signature, vm::setSignature, "Signature (type your full name)", required = true, error = errors["signature"])
            FormField(state.signedOn, vm::setSignedOn, "Date", required = true, error = errors["date"], placeholder = "YYYY-MM-DD", help = "Format: YYYY-MM-DD")
        }
        val c1 = state.centers.firstOrNull { it.id.toString() == state.center1 }
        val docs = program.documents.filter { state.files[it.id]?.url != null }
        FormSection("Application review", subtitle = "Check your details before submitting.") {
            val rows = listOf(
                "Applicant" to listOf(state.personal["full_name"], state.personal["email"]).filter { !it.isNullOrBlank() }.joinToString(" · "),
                "Category" to (state.category?.let { "${it.name}: ${it.shortLabel}" } ?: ""),
                "Primary centre" to (c1?.label ?: ""),
                "Documents" to docs.joinToString(", ") { it.name }
            )
            rows.forEach { (k, v) ->
                Column {
                    Text(k, style = AppTheme.typography.labelMedium)
                    Text(v.ifBlank { "Not completed" }, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading))
                }
            }
        }
    }
}
