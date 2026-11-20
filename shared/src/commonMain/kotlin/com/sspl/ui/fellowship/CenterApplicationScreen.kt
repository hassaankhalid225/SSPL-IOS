package com.sspl.ui.fellowship

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import org.koin.compose.koinInject

/**
 * E5: Register a training centre — 5-step accreditation wizard (CenterApplicationWizard.jsx).
 */
@Composable
fun CenterApplicationScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject()
) {
    val vm: CenterApplicationViewModel = viewModel { CenterApplicationViewModel(repository) }
    val state by vm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        val receipt = state.receipt
        val program = state.program
        when {
            receipt != null -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    ReceiptCard(
                        eyebrow = "Training centre application",
                        title = "Application under review",
                        rows = listOf(
                            "Reference" to receipt.id,
                            "Program" to receipt.program,
                            "Institution" to receipt.institution,
                            "Master trainer" to receipt.masterTrainer,
                            "Status" to receipt.status
                        ),
                        note = "Once approved by the administration, login credentials will be emailed to the registered address. Please do not submit duplicate applications.",
                        primaryText = "Back to fellowship",
                        onPrimary = { navController.popBackStack() },
                        secondaryText = "View accredited centres",
                        onSecondary = { navController.navigate(Screen.TrainingCentresScreen.route) }
                    )
                }
            }
            program != null -> CenterWizard(vm, state, program)
            else -> when (val p = state.programs) {
                is LoadState.Loading -> SkeletonDetail(showHero = false)
                is LoadState.Error -> ErrorState(title = "Couldn't load the application", message = p.message, onRetry = vm::loadPrograms)
                is LoadState.Ready -> if (p.data.isEmpty()) {
                    EmptyState(
                        title = "Centre registration is closed",
                        message = "Fellowship training center applications are not open currently. Please check back later or contact the Society administration.",
                        icon = Icons.Default.Apartment,
                        actionText = "Contact us",
                        onActionClick = { navController.navigate(Screen.ContactUsScreen.route) }
                    )
                } else {
                    ProgramChooser(p.data, onPick = vm::selectProgram, title = "Choose a fellowship program",
                        subtitle = "More than one program is open. Select the program you want to register your training center for.")
                }
            }
        }
        if (state.submitting) BlockingLoader("Submitting application…")
        state.dialog?.let { d ->
            AlertDialog(
                onDismissRequest = vm::dismissDialog,
                containerColor = AppTheme.colors.surfaceElevated,
                icon = {
                    IconTile(
                        Icons.Default.Apartment,
                        tint = when (d.tone) { NoteTone.Success -> AppTheme.colors.success; NoteTone.Error -> AppTheme.colors.error; NoteTone.Warning -> AppTheme.colors.warning; else -> AppTheme.colors.primary },
                        background = when (d.tone) { NoteTone.Success -> AppTheme.colors.successContainer; NoteTone.Error -> AppTheme.colors.errorContainer; NoteTone.Warning -> AppTheme.colors.warningContainer; else -> AppTheme.colors.primaryContainer }
                    )
                },
                title = { Text(d.title, style = AppTheme.typography.titleLarge.copy(color = AppTheme.colors.heading)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(d.message, style = AppTheme.typography.bodyMedium)
                        Text(d.subtext, style = AppTheme.typography.bodySmall)
                    }
                },
                confirmButton = { TextButton(onClick = vm::dismissDialog) { Text("OK", color = AppTheme.colors.primary) } }
            )
        }
    }
}

@Composable
private fun CenterWizard(vm: CenterApplicationViewModel, state: CenterApplicationUiState, program: Program) {
    val listState = rememberLazyListState()
    val errors = if (state.showErrors) vm.errorsFor(state, state.step) else emptyMap()
    val programsCount = (state.programs as? LoadState.Ready)?.data?.size ?: 1
    LaunchedEffect(state.stepIndex) { listState.scrollToItem(0) }
    LaunchedEffect(state.showErrors) { if (state.showErrors) listState.animateScrollToItem(0) }

    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "head") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (programsCount > 1) TextButtonLink(text = "All programs", onClick = vm::clearProgram)
                    Text("TRAINING CENTRE ACCREDITATION", style = AppTheme.typography.overline)
                    Text(program.name, style = AppTheme.typography.headlineSmall.copy(color = AppTheme.colors.heading))
                    if (program.shortName.isNotBlank()) Text(program.shortName, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary))
                }
            }
            item(key = "steps") {
                SsplCard {
                    StepIndicator(currentStep = state.stepIndex + 1, totalSteps = CENTER_STEPS.size, stepLabels = CENTER_STEPS.map { it.second })
                }
            }
            if (errors.isNotEmpty()) item(key = "errs") { InlineNote("Please fix the highlighted fields to continue.", tone = NoteTone.Error) }

            item(key = "step_${state.step}") {
                when (state.step) {
                    "basic" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InlineNote(
                            "If your institution has already submitted an application, please do not apply again. Once approved, login credentials will be emailed to the registered email address.",
                            tone = NoteTone.Warning,
                            title = "Already applied?"
                        )
                        FormSection("Centre & master trainer", eyebrow = "Section A") {
                            fun f(key: String, label: String, required: Boolean = false, placeholder: String? = null,
                                  kb: KeyboardType = KeyboardType.Text, multiline: Boolean = false) = Triple(key, label, Quad(required, placeholder, kb, multiline))
                            listOf(
                                f("institution_name", "Name of institution / hospital", true, "e.g., Services Hospital / Mayo Hospital Lahore"),
                                f("unit_department", "Name of unit / department", placeholder = "e.g., Surgical Unit II"),
                                f("master_trainer_name", "Name of proposed master trainer", true, "Prof. / Dr. Full Name"),
                                f("pmdc_number", "PMDC registration number", true, "e.g., 12345-P"),
                                f("qualifications", "Qualification(s)", placeholder = "MBBS, FCPS, FRCS"),
                                f("designation", "Current designation", placeholder = "Professor & Head of Surgery"),
                                f("years_practice", "Years of laparoscopic practice", placeholder = "e.g., 12 years"),
                                f("mobile", "Mobile number", true, "03XX XXXXXXX", KeyboardType.Phone),
                                f("whatsapp", "WhatsApp number", placeholder = "03XX XXXXXXX", kb = KeyboardType.Phone),
                                f("email", "Official / contact email address", true, "trainer@hospital.edu.pk", KeyboardType.Email),
                                f("hospital_address", "Hospital postal / physical address", placeholder = "Street, hospital campus, city", multiline = true),
                            ).forEach { (key, label, q) ->
                                FormField(
                                    value = state.basic[key].orEmpty(), onValueChange = { vm.setBasic(key, it) },
                                    label = label, required = q.a, placeholder = q.b, keyboardType = q.c, multiline = q.d, error = errors[key]
                                )
                            }
                        }
                    }
                    "facilities" -> FormSection(
                        "Centre facilities & equipment", eyebrow = "Section B",
                        subtitle = "Check all surgical equipment, OT setups and infrastructure available at your institution."
                    ) {
                        val options = (state.availableFacilities + state.facilities).distinct()
                        CheckGroup(options, state.facilities, vm::toggleFacility, error = errors["facilities"])
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FormField(state.customFacility, vm::setCustomFacility, "Add another facility",
                                placeholder = "e.g., 4K camera, dual console monitor", modifier = Modifier.weight(1f))
                        }
                        SecondaryButton(text = "Add facility", onClick = vm::addCustomFacility, icon = Icons.Default.Add, enabled = state.customFacility.isNotBlank())
                    }
                    "cases" -> FormSection(
                        program.let { "Case volume & exposure" }, eyebrow = "Section C",
                        subtitle = "Indicate operative volume and the hands-on surgical exposure fellows will receive."
                    ) {
                        FormField(state.caseVolume["per_week"].orEmpty(), { vm.setCase("per_week", it) }, "Laparoscopic cases per week", placeholder = "e.g., 5-8")
                        FormField(state.caseVolume["per_month"].orEmpty(), { vm.setCase("per_month", it) }, "Laparoscopic cases per month", placeholder = "e.g., 20-30")
                        FormField(state.caseVolume["last_12_months"].orEmpty(), { vm.setCase("last_12_months", it) }, "Total laparoscopic cases in last 12 months", placeholder = "e.g., 250+")
                        PillChoice(listOf("Yes", "No"), state.caseVolume["hands_on_exposure"], { vm.setCase("hands_on_exposure", it) },
                            label = "Will fellows receive hands-on surgical exposure under supervision?", required = true, error = errors["hands_on_exposure"])
                        Text("Society fellows must obtain supervised hands-on participation as per curriculum guidelines.", style = AppTheme.typography.bodySmall)
                        FormField(state.caseVolume["fellows_per_year"].orEmpty(), { vm.setCase("fellows_per_year", it) }, "Fellows your center can train per year", placeholder = "e.g., 2 fellows")
                        CheckGroup(CENTER_PARTICIPATION, state.participation, vm::toggleParticipation,
                            label = "Fellow participation opportunities in unit", help = "Check all surgical steps fellows will be involved in.")
                    }
                    "academic" -> FormSection("Academic environment", eyebrow = "Section D") {
                        PillChoice(listOf("Yes", "No"), state.regularActivities, vm::setRegular,
                            label = "Does your surgical unit conduct regular academic activities?", required = true, error = errors["regular_activities"])
                        if (state.regularActivities == "Yes") {
                            CheckGroup(CENTER_ACADEMIC_ACTIVITIES, state.activities, vm::toggleActivity,
                                label = "Regular academic activities conducted", help = "Select all activities regularly scheduled in your department.")
                        }
                    }
                    "declaration" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSection("Official undertaking", eyebrow = "Section E", subtitle = "Review all details and confirm your formal undertaking before sending.") {
                            Text(
                                "I solemnly undertake that the information provided is true, that my center has adequate infrastructure and academic environment, and that I agree to abide by all Society policies regarding this fellowship program.",
                                style = AppTheme.typography.bodyMedium
                            )
                            OptionRow("I confirm this undertaking.", checked = state.agreed, radio = false) { vm.setAgreed(!state.agreed) }
                            FieldError(errors["agreed"])
                            FormField(state.applicantName, vm::setApplicant, "Name of applicant / trainer", required = true, placeholder = "Prof. / Dr. Full Name", error = errors["applicant_name"])
                            FormField(state.signature, vm::setSignature, "Digital signature (type full name)", required = true, placeholder = "Full name as signature", error = errors["signature"])
                            FormField(state.date, vm::setDate, "Date", required = true, placeholder = "YYYY-MM-DD", error = errors["date"])
                            FormField(state.stamp, vm::setStamp, "Official stamp / reference (optional)")
                        }
                        FormSection("Summary") {
                            listOf(
                                "Institution" to state.basic["institution_name"].orEmpty(),
                                "Master trainer" to state.basic["master_trainer_name"].orEmpty(),
                                "Facilities" to "${state.facilities.size} selected",
                                "Hands-on exposure" to state.caseVolume["hands_on_exposure"].orEmpty(),
                                "Academic activities" to state.regularActivities
                            ).forEachIndexed { i, (k, v) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(k, style = AppTheme.typography.labelMedium)
                                        Text(v.ifBlank { "Not completed" }, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading))
                                    }
                                    TextButtonLink("Edit", onClick = { vm.goTo(listOf(0, 0, 1, 2, 3)[i]) })
                                }
                            }
                        }
                    }
                }
            }
        }
        WizardBar(
            isFirst = state.stepIndex == 0,
            isLast = state.stepIndex == CENTER_STEPS.lastIndex,
            onBack = vm::back,
            onNext = vm::next,
            loading = state.submitting,
            submitText = "Submit application"
        )
    }
}

private data class Quad(val a: Boolean, val b: String?, val c: KeyboardType, val d: Boolean)
