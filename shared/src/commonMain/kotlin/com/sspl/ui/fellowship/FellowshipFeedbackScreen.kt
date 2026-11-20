package com.sspl.ui.fellowship

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import org.koin.compose.koinInject

private class FbItem(val key: String, val errKeys: List<String> = emptyList(), val content: @Composable () -> Unit)

/**
 * E8: Fellow feedback & post-fellowship impact questionnaire (Part 1 A–D, Part 2 E–G).
 */
@Composable
fun FellowshipFeedbackScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject()
) {
    val vm: FellowshipFeedbackViewModel = viewModel { FellowshipFeedbackViewModel(repository) }
    val state by vm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        when (val l = state.load) {
            is FeedbackLoad.Loading -> SkeletonDetail()
            is FeedbackLoad.Error -> ErrorState(title = "Feedback form didn't load", message = l.message, onRetry = vm::load)
            is FeedbackLoad.NotEligible -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FellowshipHero("Fellowship feedback", FeedbackSchema.TITLE, null, icon = Icons.Default.Feedback)
                SsplCard {
                    EmptyState(
                        title = "Not available yet", message = l.message, icon = Icons.Default.Lock,
                        actionText = "Explore fellowships", onActionClick = { navController.navigate(Screen.FellowshipHubScreen.route) }
                    )
                }
            }
            is FeedbackLoad.Ready -> FeedbackForm(vm, state)
        }
        if (state.submitting) BlockingLoader("Submitting feedback…")
        state.success?.let { s ->
            AlertDialog(
                onDismissRequest = vm::dismissSuccess,
                containerColor = AppTheme.colors.surfaceElevated,
                icon = { IconTile(Icons.Default.EmojiEvents, tint = AppTheme.colors.success, background = AppTheme.colors.successContainer) },
                title = { Text(s.title, style = AppTheme.typography.titleLarge.copy(color = AppTheme.colors.heading)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.message, style = AppTheme.typography.bodyMedium)
                        Text(s.subtext, style = AppTheme.typography.bodySmall)
                    }
                },
                confirmButton = { TextButton(onClick = vm::dismissSuccess) { Text("Done", color = AppTheme.colors.primary) } }
            )
        }
    }
}

@Composable
private fun FeedbackForm(vm: FellowshipFeedbackViewModel, state: FellowshipFeedbackUiState) {
    val listState = rememberLazyListState()
    val st = state.status
    val e = state.errors
    val items = buildList {
        add(FbItem("header") { Header(state) })
        add(FbItem("parts") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SegmentedTabs(options = listOf("Part 1 · Immediate", "Part 2 · 6-month"), selectedIndex = state.activePart - 1, onSelect = { vm.setPart(it + 1) })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatusBadge(if (st.part1Completed) "Submitted" else "Pending", variant = if (st.part1Completed) BadgeVariant.SUCCESS else BadgeVariant.WARNING)
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatusBadge(
                            when { st.part2Completed -> "Submitted"; st.part2CanFill -> "Unlocked"; else -> "Locked" },
                            variant = when { st.part2Completed -> BadgeVariant.SUCCESS; st.part2CanFill -> BadgeVariant.WARNING; else -> BadgeVariant.NEUTRAL }
                        )
                    }
                }
            }
        })
        state.serverError?.let { add(FbItem("serverErr") { InlineNote(it, tone = NoteTone.Error, title = "Submission failed") }) }
        if (e.isNotEmpty()) add(FbItem("errSummary") { InlineNote("${e.size} question(s) need an answer. They're highlighted below.", tone = NoteTone.Error) })
        if (state.activePart == 1) part1Items(vm, state) else part2Items(vm, state)
    }

    LaunchedEffect(state.errorTick) {
        if (state.errorTick > 0) {
            val first = state.errors.keys.firstOrNull() ?: return@LaunchedEffect
            val idx = items.indexOfFirst { first in it.errKeys }
            listState.animateScrollToItem(if (idx >= 0) idx else 0)
        }
    }
    LaunchedEffect(state.activePart) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.key }) { it.content() }
    }
}

@Composable
private fun Header(state: FellowshipFeedbackUiState) {
    var showInstructions by rememberSaveable { mutableStateOf(!state.status.part1Completed) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FellowshipHero(FeedbackSchema.ORG, FeedbackSchema.TITLE, state.programName, icon = Icons.Default.Feedback)
        InlineNote(
            if (state.status.bothCompleted) "Questionnaire completed — eligible for certificate." else "Required for certificate issuance.",
            tone = if (state.status.bothCompleted) NoteTone.Success else NoteTone.Warning
        )
        SsplCard {
            Row(
                Modifier.fillMaxWidth().clickable { showInstructions = !showInstructions }.heightIn(min = 48.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Purpose & instructions", style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading), modifier = Modifier.weight(1f))
                Icon(if (showInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = AppTheme.colors.muted)
            }
            AnimatedVisibility(showInstructions) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Purpose: ${FeedbackSchema.PURPOSE}", style = AppTheme.typography.bodyMedium)
                    Text("Instructions: ${FeedbackSchema.INSTRUCTIONS}", style = AppTheme.typography.bodyMedium)
                    Text("Note: ${FeedbackSchema.NOTE}", style = AppTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- building blocks

@Composable
private fun QCard(title: String, error: String?, content: @Composable ColumnScope.() -> Unit) {
    val colors = AppTheme.colors
    SsplCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = AppTheme.typography.titleSmall.copy(color = if (error != null) colors.error else colors.heading))
            content()
            FieldError(error)
        }
    }
}

@Composable
private fun SectionTitle(text: String, sub: String? = null) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(text.uppercase(), style = AppTheme.typography.overline)
        if (sub != null) Text(sub, style = AppTheme.typography.bodySmall)
    }
}

@Composable
private fun ScaleRow(label: String, value: Int?, onChange: (Int) -> Unit, enabled: Boolean, error: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = AppTheme.typography.bodyMedium.copy(color = if (error) AppTheme.colors.error else AppTheme.colors.heading))
        ScaleButtons(value, onChange, enabled = enabled, error = error)
    }
}

// ---------------------------------------------------------------- Part 1

private fun MutableList<FbItem>.part1Items(vm: FellowshipFeedbackViewModel, state: FellowshipFeedbackUiState) {
    val p = state.part1
    val e = state.errors
    val locked = state.status.part1Completed
    val en = !locked
    if (locked) add(FbItem("p1locked") { InlineNote("Part 1 has been submitted. Your answers are shown below for reference.", tone = NoteTone.Success) })

    add(FbItem("secA") { SectionTitle("Section A – Fellow profile") })
    add(FbItem("age", listOf("age")) {
        QCard("1. Age *", e["age"]) {
            FormField(p.text("section_a", "age"), { vm.set1("age", "section_a", "age", value = it.filter(Char::isDigit)) }, "Age", keyboardType = KeyboardType.Number, enabled = en)
        }
    })
    add(FbItem("gender", listOf("gender")) {
        QCard("2. Gender *", e["gender"]) { PillChoice(FeedbackSchema.GENDER, p.text("section_a", "gender"), { vm.set1("gender", "section_a", "gender", value = it) }, enabled = en) }
    })
    add(FbItem("qualification", listOf("qualification")) {
        QCard("3. Qualification *", e["qualification"]) {
            PillChoice(FeedbackSchema.QUALIFICATION, p.text("section_a", "qualification"), { vm.set1("qualification", "section_a", "qualification", value = it) }, enabled = en)
            if (p.text("section_a", "qualification") == "Other") FormField(p.text("section_a", "qualification_other"), { vm.set1("qualification", "section_a", "qualification_other", value = it) }, "Please specify", enabled = en)
        }
    })
    add(FbItem("designation", listOf("current_designation")) {
        QCard("4. Current designation *", e["current_designation"]) {
            ChoiceGroup(FeedbackSchema.DESIGNATION, p.text("section_a", "current_designation"), { vm.set1("current_designation", "section_a", "current_designation", value = it) }, enabled = en)
            if (p.text("section_a", "current_designation") == "Other") FormField(p.text("section_a", "designation_other"), { vm.set1("current_designation", "section_a", "designation_other", value = it) }, "Please specify", enabled = en)
        }
    })
    listOf(
        Triple("current_institution_city", "5. Current institution and city *", "Institution, city"),
        Triple("training_centre", "6. Fellowship training centre *", "Training centre"),
        Triple("supervisor_name", "7. Name of supervisor *", "Supervisor"),
    ).forEach { (k, title, label) ->
        add(FbItem(k, listOf(k)) { QCard(title, e[k]) { FormField(p.text("section_a", k), { vm.set1(k, "section_a", k, value = it) }, label, enabled = en) } })
    }
    add(FbItem("duration", listOf("duration")) {
        QCard("8. Duration of fellowship *", e["duration"]) {
            PillChoice(FeedbackSchema.DURATION, p.text("section_a", "duration"), { vm.set1("duration", "section_a", "duration", value = it) }, enabled = en)
            if (p.text("section_a", "duration") == "Other") FormField(p.text("section_a", "duration_other"), { vm.set1("duration", "section_a", "duration_other", value = it) }, "Please specify", enabled = en)
        }
    })

    add(FbItem("secB") { SectionTitle("Section B – Previous laparoscopic experience") })
    add(FbItem("prevCases", listOf("previous_cases_count")) {
        QCard("9. Before joining, approximately how many laparoscopic cholecystectomies had you performed? *", e["previous_cases_count"]) {
            PillChoice(FeedbackSchema.PREVIOUS_CASES, p.text("section_b", "previous_cases_count"), { vm.set1("previous_cases_count", "section_b", "previous_cases_count", value = it) }, enabled = en)
        }
    })
    add(FbItem("prevLevel", listOf("previous_experience_level")) {
        QCard("10. Before the fellowship, your level of experience was: *", e["previous_experience_level"]) {
            ChoiceGroup(FeedbackSchema.PREVIOUS_LEVEL, p.text("section_b", "previous_experience_level"), { vm.set1("previous_experience_level", "section_b", "previous_experience_level", value = it) }, enabled = en)
        }
    })
    add(FbItem("reasons", listOf("joining_reasons")) {
        QCard("11. What were your main reasons for joining? (Select all that apply.) *", e["joining_reasons"]) {
            CheckGroup(FeedbackSchema.JOINING_REASONS, p.list("section_b", "joining_reasons"), { vm.toggle1("joining_reasons", "section_b", "joining_reasons", option = it) }, enabled = en)
            if ("Other" in p.list("section_b", "joining_reasons")) FormField(p.text("section_b", "joining_reasons_other"), { vm.set1("joining_reasons", "section_b", "joining_reasons_other", value = it) }, "Other reason", enabled = en)
        }
    })

    add(FbItem("secC") { SectionTitle("Section C – Training experience") })
    val ratingKeys = FeedbackSchema.SECTION_C.map { "rating_${it.first}" }
    add(FbItem("ratings", ratingKeys) {
        QCard("12. How would you rate the following aspects of your fellowship? *", ratingKeys.firstNotNullOfOrNull { e[it] }?.let { "Please rate every aspect (1–5)." }) {
            Text(FeedbackSchema.RATING_SCALE, style = AppTheme.typography.bodySmall)
            FeedbackSchema.SECTION_C.forEach { (k, label) ->
                ScaleRow(label, p.number("section_c", "ratings", k), { vm.setNum1("rating_$k", "section_c", "ratings", k, value = it) }, en, e["rating_$k"] != null)
            }
        }
    })
    val caseKeys = FeedbackSchema.CASE_ROWS.map { "case_count_${it.first}" }
    add(FbItem("caseCounts", caseKeys) {
        QCard("13. Approximately how many laparoscopic cholecystectomies did you: *", null) {
            FeedbackSchema.CASE_ROWS.forEach { (k, label) ->
                FormField(
                    p.text("section_c", "case_counts", k), { vm.set1("case_count_$k", "section_c", "case_counts", k, value = it.filter(Char::isDigit)) },
                    label, keyboardType = KeyboardType.Number, error = e["case_count_$k"], enabled = en
                )
            }
        }
    })
    val wsKeys = FeedbackSchema.WORKSHOPS.map { "workshop_${it.first}" }
    add(FbItem("workshops", wsKeys) {
        QCard("14. Did you attend the mandatory workshops? *", null) {
            FeedbackSchema.WORKSHOPS.forEach { (k, label) ->
                PillChoice(listOf("Yes", "No"), p.text("section_c", "workshops", k), { vm.set1("workshop_$k", "section_c", "workshops", k, value = it) },
                    label = label, error = e["workshop_$k"], enabled = en)
            }
        }
    })
    add(FbItem("freq", listOf("constructive_feedback_frequency")) {
        QCard("15. Did you receive constructive feedback after your operative procedures? *", e["constructive_feedback_frequency"]) {
            PillChoice(FeedbackSchema.FEEDBACK_FREQUENCY, p.text("section_c", "constructive_feedback_frequency"), { vm.set1("constructive_feedback_frequency", "section_c", "constructive_feedback_frequency", value = it) }, enabled = en)
        }
    })
    add(FbItem("valuable", listOf("most_valuable_aspect")) {
        QCard("16. What was the most valuable aspect of your fellowship? *", e["most_valuable_aspect"]) {
            FormField(p.text("section_c", "most_valuable_aspect"), { vm.set1("most_valuable_aspect", "section_c", "most_valuable_aspect", value = it) }, "Your answer", multiline = true, enabled = en)
        }
    })
    add(FbItem("improve", listOf("aspects_to_improve")) {
        QCard("17. What aspects of the fellowship require improvement? *", e["aspects_to_improve"]) {
            FormField(p.text("section_c", "aspects_to_improve"), { vm.set1("aspects_to_improve", "section_c", "aspects_to_improve", value = it) }, "Your answer", multiline = true, enabled = en)
        }
    })

    add(FbItem("secD") { SectionTitle("Section D – Self-assessment of surgical skills", "Rate your confidence BEFORE and AFTER the fellowship. ${FeedbackSchema.CONFIDENCE_SCALE}") })
    FeedbackSchema.SKILLS.forEach { (k, label) ->
        add(FbItem("skill_$k", listOf("skill_$k")) {
            QCard(label, e["skill_$k"]) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Before", style = AppTheme.typography.labelMedium, modifier = Modifier.width(52.dp))
                    ScaleButtons(p.number("section_d", "skills", k, "before"), { vm.setNum1("skill_$k", "section_d", "skills", k, "before", value = it) }, Modifier.weight(1f), enabled = en)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("After", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold), modifier = Modifier.width(52.dp))
                    ScaleButtons(p.number("section_d", "skills", k, "after"), { vm.setNum1("skill_$k", "section_d", "skills", k, "after", value = it) }, Modifier.weight(1f), enabled = en)
                }
            }
        })
    }
    add(FbItem("overall", listOf("overall_skill_improvement")) {
        QCard("18. Overall, how much have your laparoscopic surgical skills improved? *", e["overall_skill_improvement"]) {
            ChoiceGroup(FeedbackSchema.SKILL_IMPROVEMENT, p.text("section_d", "overall_skill_improvement"), { vm.set1("overall_skill_improvement", "section_d", "overall_skill_improvement", value = it) }, enabled = en)
        }
    })
    add(FbItem("straight", listOf("confident_straightforward")) {
        QCard("19. Do you now feel confident performing a straightforward laparoscopic cholecystectomy independently? *", e["confident_straightforward"]) {
            ChoiceGroup(FeedbackSchema.STRAIGHTFORWARD, p.text("section_d", "confident_straightforward"), { vm.set1("confident_straightforward", "section_d", "confident_straightforward", value = it) }, enabled = en)
        }
    })
    add(FbItem("difficult", listOf("confident_difficult")) {
        QCard("20. Do you feel confident managing difficult laparoscopic cholecystectomy cases? *", e["confident_difficult"]) {
            ChoiceGroup(FeedbackSchema.DIFFICULT, p.text("section_d", "confident_difficult"), { vm.set1("confident_difficult", "section_d", "confident_difficult", value = it) }, enabled = en)
        }
    })
    if (!locked) add(FbItem("submit1") { PrimaryButton(text = "Submit Part 1 (immediate feedback)", onClick = vm::submitPart1, loading = state.submitting) })
}

// ---------------------------------------------------------------- Part 2

private fun MutableList<FbItem>.part2Items(vm: FellowshipFeedbackViewModel, state: FellowshipFeedbackUiState) {
    val st = state.status
    if (!st.part1Completed || (!st.part2CanFill && !st.part2Completed)) {
        add(FbItem("p2locked") {
            SsplCard {
                EmptyState(
                    title = if (!st.part1Completed) "Complete Part 1 first" else "Part 2 scheduled 6 months post-fellowship",
                    message = if (!st.part1Completed) "Please submit Part 1 (Sections A–D) before the 6-month post-fellowship impact questionnaire."
                    else "Part 2 evaluates the real-world impact of your fellowship 6 months after completion" +
                        (st.part2UnlockDate?.let { " (scheduled unlock: ${prettyDate(it)})" } ?: "") + ".",
                    icon = Icons.Default.Lock,
                    actionText = "Go to Part 1",
                    onActionClick = { vm.setPart(1) }
                )
            }
        })
        return
    }
    val p = state.part2
    val e = state.errors
    val en = !st.part2Completed
    if (st.part2Completed) add(FbItem("p2done") { InlineNote("Part 2 has been submitted. Thank you for completing the questionnaire.", tone = NoteTone.Success) })

    fun radio(key: String, num: String, q: String, opts: List<String>, section: String = "section_e", pills: Boolean = false) {
        add(FbItem(key, listOf(key)) {
            QCard("$num. $q", e[key]) {
                if (pills) PillChoice(opts, p.text(section, key), { vm.set2(key, section, key, value = it) }, enabled = en)
                else ChoiceGroup(opts, p.text(section, key), { vm.set2(key, section, key, value = it) }, enabled = en)
            }
        })
    }
    fun text(key: String, num: String, q: String, section: String) {
        add(FbItem(key, listOf(key)) {
            QCard("$num. $q", e[key]) { FormField(p.text(section, key), { vm.set2(key, section, key, value = it) }, "Your answer", multiline = true, enabled = en) }
        })
    }

    add(FbItem("secE") { SectionTitle("Section E – Impact on independent surgical practice") })
    radio("started_independent", "21", "Have you started performing laparoscopic cholecystectomies independently after completing the fellowship? *", listOf("Yes", "No"), pills = true)
    if (p.text("section_e", "started_independent") == "Yes") {
        add(FbItem("practice_location", listOf("practice_location")) {
            QCard("22. If yes, where are you currently performing them?", e["practice_location"]) {
                ChoiceGroup(FeedbackSchema.PRACTICE_LOCATION, p.text("section_e", "practice_location"), { vm.set2("practice_location", "section_e", "practice_location", value = it) }, enabled = en)
                if (p.text("section_e", "practice_location") == "Other") FormField(p.text("section_e", "practice_location_other"), { vm.set2("practice_location", "section_e", "practice_location_other", value = it) }, "Please specify", enabled = en)
            }
        })
    }
    radio("independent_cases_count", "23", "Approximately how many laparoscopic cholecystectomies have you performed independently since completing the fellowship? *", FeedbackSchema.INDEPENDENT_CASES, pills = true)
    radio("regular_part_of_practice", "24", "Are you currently offering laparoscopic cholecystectomy as a regular part of your surgical practice? *", FeedbackSchema.REGULAR_PART)
    radio("institution_service_impact", "25", "Has the fellowship enabled you to introduce or expand laparoscopic surgical services at your institution? *", FeedbackSchema.INSTITUTION_SERVICE)
    radio("private_practice_impact", "26", "Has the fellowship helped you establish or expand your independent private surgical practice? *", FeedbackSchema.PRIVATE_PRACTICE)
    add(FbItem("barriers", listOf("barriers")) {
        QCard("27. If you have not started performing laparoscopic cholecystectomies independently, what are the main barriers?", e["barriers"]) {
            CheckGroup(FeedbackSchema.BARRIERS, p.list("section_e", "barriers"), { vm.toggle2("barriers", "section_e", "barriers", option = it) }, enabled = en)
            if ("Other" in p.list("section_e", "barriers")) FormField(p.text("section_e", "barriers_other"), { vm.set2("barriers", "section_e", "barriers_other", value = it) }, "Other barrier", enabled = en)
        }
    })
    radio("changed_clinical_decision_making", "28", "Has your confidence in managing difficult gallbladder cases improved sufficiently to change your clinical decision-making? *", FeedbackSchema.DECISION_MAKING, pills = true)

    add(FbItem("secF") { SectionTitle("Section F – Overall assessment") })
    radio("would_recommend", "29", "Would you recommend this fellowship to other surgeons? *", FeedbackSchema.RECOMMEND, "section_f", pills = true)
    radio("duration_adequate", "30", "Do you think the duration of the fellowship was adequate? *", FeedbackSchema.DURATION_ADEQUATE, "section_f")
    text("additional_training_recommended", "31", "What additional training would you recommend? *", "section_f")
    text("most_important_change", "32", "In your opinion, what is the single most important change required to improve this fellowship? *", "section_f")

    add(FbItem("secG") { SectionTitle("Section G – Final reflections") })
    text("helped_professionally_by", "33", "Complete the statement: “The ${FeedbackSchema.PROGRAM} has helped me professionally by:” *", "section_g")
    add(FbItem("additional_comments") {
        QCard("34. Any additional comments or suggestions:", null) {
            FormField(p.text("section_g", "additional_comments"), { vm.set2("additional_comments", "section_g", "additional_comments", value = it) }, "Your answer (optional)", multiline = true, enabled = en)
        }
    })
    if (!st.part2Completed) add(FbItem("submit2") { PrimaryButton(text = "Submit Part 2 (impact questionnaire)", onClick = vm::submitPart2, loading = state.submitting) })
}

