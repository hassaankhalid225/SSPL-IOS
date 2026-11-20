package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.FellowshipApiException
import com.sspl.core.repositories.FellowshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

// ---------------------------------------------------------------- schema (fellowshipFeedbackSchema.js)

object FeedbackSchema {
    const val TITLE = "Fellow Feedback and Post-Fellowship Impact Questionnaire"
    const val ORG = "SOCIETY OF SURGEONS – LAHORE CHAPTER"
    const val PROGRAM = "Mini Fellowship in Laparoscopic Cholecystectomy"
    const val PURPOSE = "To evaluate the quality of training, improvement in laparoscopic surgical skills, confidence in performing laparoscopic cholecystectomy, and the impact of the fellowship on independent surgical practice."
    const val INSTRUCTIONS = "Please answer honestly. Your responses will be used for programme evaluation and may contribute to future research. Individual responses will be kept confidential and reported in an anonymised manner."
    const val NOTE = "This questionnaire is divided into two parts — Part 1 (Sections A–D) can be completed immediately after finishing fellowship training, and Part 2 (Sections E–G) is completed 6 months post-fellowship. Your Fellowship Certificate is issued once both parts are completed."

    val GENDER = listOf("Male", "Female", "Prefer not to say")
    val QUALIFICATION = listOf("FCPS Surgery", "MS Surgery", "Other")
    val DESIGNATION = listOf("Senior Registrar", "Assistant Professor", "Associate Professor", "Consultant", "Other")
    val DURATION = listOf("6 weeks", "2 months", "3 months", "Other")
    val PREVIOUS_CASES = listOf("None", "1–10", "11–25", "26–50", "51–100", "More than 100")
    val PREVIOUS_LEVEL = listOf(
        "No practical laparoscopic experience.",
        "Could assist in laparoscopic cholecystectomy.",
        "Could perform basic steps under supervision.",
        "Could perform straightforward cases but needed help in difficult cases.",
        "Could independently perform most routine laparoscopic cholecystectomies.",
    )
    val JOINING_REASONS = listOf(
        "Improve laparoscopic skills.",
        "Gain confidence in performing laparoscopic cholecystectomy.",
        "Learn management of difficult gallbladders.",
        "Learn management of intraoperative complications.",
        "Establish laparoscopic services at my institution.",
        "Start independent laparoscopic surgical practice.",
        "Other",
    )
    const val RATING_SCALE = "1 = Very poor · 2 = Poor · 3 = Satisfactory · 4 = Good · 5 = Excellent"
    val SECTION_C = listOf(
        "quality_of_supervision" to "Quality of supervision",
        "or_exposure" to "Availability of operating room exposure",
        "cases_performed" to "Number of cases performed",
        "opportunity_to_perform" to "Opportunity to perform the operation yourself",
        "stepwise_teaching" to "Stepwise teaching of the procedure",
        "supervisor_feedback" to "Feedback from supervisor",
        "difficult_gallbladders" to "Exposure to difficult gallbladders",
        "intraop_complications" to "Exposure to intraoperative complications",
        "learning_environment" to "Overall learning environment",
        "overall_experience" to "Overall fellowship experience",
    )
    val CASE_ROWS = listOf(
        "observed" to "Observe",
        "assisted" to "Assist in",
        "performed_partial_supervision" to "Perform partially under supervision",
        "performed_complete_supervision" to "Perform completely under supervision",
        "performed_minimal_assistance" to "Perform with the supervisor scrubbed but providing minimal assistance",
    )
    val WORKSHOPS = listOf(
        "safe_lap_chole" to "Safe Laparoscopic Cholecystectomy",
        "cbd_injuries" to "Management of CBD Injuries",
        "online_module" to "Online Laparoscopic Cholecystectomy Module",
    )
    val FEEDBACK_FREQUENCY = listOf("Always", "Frequently", "Sometimes", "Rarely", "Never")
    const val CONFIDENCE_SCALE = "1 = Not confident at all · 2 = Slightly · 3 = Moderately · 4 = Very confident · 5 = Fully confident / independent"
    val SKILLS = listOf(
        "operating_instruments" to "Operating laparoscopic instruments",
        "camera_navigation" to "Camera navigation and orientation",
        "pneumoperitoneum" to "Establishing pneumoperitoneum",
        "port_placement" to "Port placement",
        "calots_triangle" to "Retraction and exposure of Calot’s triangle",
        "cystic_duct_artery_id" to "Identification of cystic duct and artery",
        "critical_view_of_safety" to "Achieving Critical View of Safety",
        "clipping_and_division" to "Clipping and division of cystic duct and artery",
        "liver_bed_dissection" to "Dissection of gallbladder from liver bed",
        "haemostasis" to "Haemostasis",
        "gallbladder_retrieval" to "Retrieval of gallbladder",
        "port_closure" to "Closure of port sites",
        "difficult_gallbladders" to "Managing difficult gallbladders",
        "intraop_bleeding" to "Managing intraoperative bleeding",
        "bile_duct_injury" to "Recognising and managing suspected bile duct injury",
        "subtotal_cholecystectomy" to "Deciding when to perform subtotal cholecystectomy",
        "convert_to_open" to "Deciding when to convert to open surgery",
    )
    val SKILL_IMPROVEMENT = listOf("No improvement", "Minimal improvement", "Moderate improvement", "Significant improvement", "Exceptional improvement")
    val STRAIGHTFORWARD = listOf("Yes", "Yes, but only with a senior surgeon available for assistance.", "No")
    val DIFFICULT = listOf("Yes, independently.", "Yes, with senior support.", "No")
    val PRACTICE_LOCATION = listOf("Government teaching hospital.", "Private hospital.", "Private clinic / surgical practice.", "Multiple institutions.", "Other")
    val INDEPENDENT_CASES = listOf("None", "1–5", "6–10", "11–25", "26–50", "More than 50")
    val REGULAR_PART = listOf("Yes, regularly.", "Yes, occasionally.", "No.")
    val INSTITUTION_SERVICE = listOf("Yes, introduced a new service.", "Yes, expanded an existing service.", "No.", "Not applicable.")
    val PRIVATE_PRACTICE = listOf("Yes, significantly.", "Yes, to some extent.", "No.", "Not applicable.")
    val BARRIERS = listOf(
        "Lack of laparoscopic equipment.", "Lack of institutional support.", "Lack of operating room access.",
        "Lack of patient referrals.", "Lack of confidence.", "Need for further training.", "Lack of senior support.", "Other",
    )
    val DECISION_MAKING = listOf("Yes", "Partially", "No")
    val RECOMMEND = listOf("Definitely", "Probably", "Not sure", "Probably not", "Definitely not")
    val DURATION_ADEQUATE = listOf("Yes", "No, it should be longer.", "No, it should be shorter.", "It should be competency-based rather than time-based.")
}

// ---------------------------------------------------------------- JSON path helpers

private fun s(v: String) = JsonPrimitive(v)

internal fun JsonObject.at(path: List<String>): JsonElement? {
    var cur: JsonElement? = this
    for (k in path) cur = (cur as? JsonObject)?.get(k) ?: return null
    return cur
}

internal fun JsonObject.with(path: List<String>, value: JsonElement): JsonObject {
    val head = path.first()
    return if (path.size == 1) JsonObject(this + (head to value))
    else JsonObject(this + (head to ((this[head] as? JsonObject) ?: JsonObject(emptyMap())).with(path.drop(1), value)))
}

internal fun JsonObject.text(vararg path: String): String =
    (at(path.toList()) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull.orEmpty()

internal fun JsonObject.number(vararg path: String): Int? =
    (at(path.toList()) as? JsonPrimitive)?.let { it.intOrNull ?: it.contentOrNull?.toIntOrNull() }

internal fun JsonObject.list(vararg path: String): List<String> =
    (at(path.toList()) as? JsonArray)?.strings().orEmpty()

private fun obj(vararg pairs: Pair<String, JsonElement>) = JsonObject(mapOf(*pairs))

private fun initialPart1(defaults: JsonObject?): JsonObject = obj(
    "section_a" to obj(
        "age" to s(""), "gender" to s(""), "qualification" to s(""), "qualification_other" to s(""),
        "current_designation" to s(""), "designation_other" to s(""),
        "current_institution_city" to s(defaults.str("current_institution").orEmpty()),
        "training_centre" to s(defaults.str("training_centre").orEmpty()),
        "supervisor_name" to s(defaults.str("supervisor_name").orEmpty()),
        "duration" to s(""), "duration_other" to s(""),
    ),
    "section_b" to obj("previous_cases_count" to s(""), "previous_experience_level" to s(""), "joining_reasons" to JsonArray(emptyList()), "joining_reasons_other" to s("")),
    "section_c" to obj(
        "ratings" to JsonObject(emptyMap()),
        "case_counts" to JsonObject(FeedbackSchema.CASE_ROWS.associate { it.first to s("") }),
        "workshops" to JsonObject(FeedbackSchema.WORKSHOPS.associate { it.first to s("") }),
        "constructive_feedback_frequency" to s(""), "most_valuable_aspect" to s(""), "aspects_to_improve" to s(""),
    ),
    "section_d" to obj("skills" to JsonObject(emptyMap()), "overall_skill_improvement" to s(""), "confident_straightforward" to s(""), "confident_difficult" to s("")),
)

private fun initialPart2(): JsonObject = obj(
    "section_e" to obj(
        "started_independent" to s(""), "practice_location" to s(""), "practice_location_other" to s(""),
        "independent_cases_count" to s(""), "regular_part_of_practice" to s(""), "institution_service_impact" to s(""),
        "private_practice_impact" to s(""), "barriers" to JsonArray(emptyList()), "barriers_other" to s(""),
        "changed_clinical_decision_making" to s(""),
    ),
    "section_f" to obj("would_recommend" to s(""), "duration_adequate" to s(""), "additional_training_recommended" to s(""), "most_important_change" to s("")),
    "section_g" to obj("helped_professionally_by" to s(""), "additional_comments" to s("")),
)

// ---------------------------------------------------------------- state

data class FeedbackStatus(
    val part1Completed: Boolean = false,
    val part2Completed: Boolean = false,
    val part2CanFill: Boolean = false,
    val part2UnlockDate: String? = null,
    val bothCompleted: Boolean = false
)

data class FeedbackSuccess(val title: String, val message: String, val subtext: String)

sealed interface FeedbackLoad {
    data object Loading : FeedbackLoad
    data class Error(val message: String) : FeedbackLoad
    data class NotEligible(val message: String) : FeedbackLoad
    data object Ready : FeedbackLoad
}

data class FellowshipFeedbackUiState(
    val load: FeedbackLoad = FeedbackLoad.Loading,
    val programName: String = FeedbackSchema.PROGRAM,
    val status: FeedbackStatus = FeedbackStatus(),
    val part1: JsonObject = initialPart1(null),
    val part2: JsonObject = initialPart2(),
    val activePart: Int = 1,
    val errors: Map<String, String> = emptyMap(),
    val submitting: Boolean = false,
    val serverError: String? = null,
    val success: FeedbackSuccess? = null,
    /** Bumped on each failed validation so the screen can scroll to the first error. */
    val errorTick: Int = 0
)

/** E8: Fellow feedback & post-fellowship impact questionnaire (FellowshipFeedbackView.jsx). */
class FellowshipFeedbackViewModel(private val repository: FellowshipRepository) : ViewModel() {

    private val _state = MutableStateFlow(FellowshipFeedbackUiState())
    val state: StateFlow<FellowshipFeedbackUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(load = FeedbackLoad.Loading) }
            runCatching { repository.myFeedback() }
                .onSuccess { res -> apply(res, initial = true) }
                .onFailure { e ->
                    val status = (e as? FellowshipApiException)?.status
                    _state.update {
                        it.copy(load = if (status == 403 || status == 404)
                            FeedbackLoad.NotEligible("The feedback questionnaire becomes available once you are enrolled on a fellowship.")
                        else FeedbackLoad.Error(e.userMessage("Feedback form could not be loaded.")))
                    }
                }
        }
    }

    private fun apply(res: JsonObject, initial: Boolean) {
        val st = res.obj("state") ?: res.obj("data").obj("state")
        if (st == null && !initial) {
            // Response without a state envelope: mark the part we just sent as done.
            _state.update {
                val s = it.status
                it.copy(load = FeedbackLoad.Ready, status = if (!s.part1Completed) s.copy(part1Completed = true) else s.copy(part2Completed = true, bothCompleted = true))
            }
            return
        }
        val status = FeedbackStatus(
            st.bool("part1_completed") == true, st.bool("part2_completed") == true, st.bool("part2_can_fill") == true,
            st.str("part2_unlock_date"), st.bool("both_completed") == true
        )
        val fb = res.obj("feedback")
        _state.update {
            it.copy(
                load = FeedbackLoad.Ready,
                status = status,
                programName = res.obj("defaults").str("program_name") ?: it.programName,
                part1 = if (initial) fb.obj("part1_data") ?: initialPart1(res.obj("defaults")) else it.part1,
                part2 = if (initial) fb.obj("part2_data") ?: initialPart2() else it.part2,
                activePart = if (initial && status.part1Completed && !status.part2Completed) 2 else it.activePart
            )
        }
    }

    fun setPart(part: Int) = _state.update { it.copy(activePart = part, errors = emptyMap()) }
    fun dismissSuccess() = _state.update { it.copy(success = null) }

    private fun clear(key: String) = _state.update { if (key in it.errors) it.copy(errors = it.errors - key) else it }

    fun set1(errKey: String, vararg path: String, value: String) { _state.update { it.copy(part1 = it.part1.with(path.toList(), s(value))) }; clear(errKey) }
    fun setNum1(errKey: String, vararg path: String, value: Int) { _state.update { it.copy(part1 = it.part1.with(path.toList(), JsonPrimitive(value))) }; clear(errKey) }
    fun toggle1(errKey: String, vararg path: String, option: String) {
        _state.update {
            val cur = it.part1.list(*path)
            it.copy(part1 = it.part1.with(path.toList(), JsonArray((if (option in cur) cur - option else cur + option).map(::s))))
        }
        clear(errKey)
    }
    fun set2(errKey: String, vararg path: String, value: String) { _state.update { it.copy(part2 = it.part2.with(path.toList(), s(value))) }; clear(errKey) }
    fun toggle2(errKey: String, vararg path: String, option: String) {
        _state.update {
            val cur = it.part2.list(*path)
            it.copy(part2 = it.part2.with(path.toList(), JsonArray((if (option in cur) cur - option else cur + option).map(::s))))
        }
        clear(errKey)
    }

    // ---- validation (collectPart1Errors / collectPart2Errors)
    private fun part1Errors(p: JsonObject): Map<String, String> {
        val e = linkedMapOf<String, String>()
        fun a(k: String) = p.text("section_a", k).trim()
        if (a("age").isEmpty()) e["age"] = "Please enter your age."
        if (a("gender").isEmpty()) e["gender"] = "Please select your gender."
        if (a("qualification").isEmpty()) e["qualification"] = "Please select your qualification."
        else if (a("qualification") == "Other" && a("qualification_other").isEmpty()) e["qualification"] = "Please specify your qualification."
        if (a("current_designation").isEmpty()) e["current_designation"] = "Please select your current designation."
        else if (a("current_designation") == "Other" && a("designation_other").isEmpty()) e["current_designation"] = "Please specify your designation."
        if (a("current_institution_city").isEmpty()) e["current_institution_city"] = "Please enter your current institution and city."
        if (a("training_centre").isEmpty()) e["training_centre"] = "Please enter your fellowship training centre."
        if (a("supervisor_name").isEmpty()) e["supervisor_name"] = "Please enter your supervisor's name."
        if (a("duration").isEmpty()) e["duration"] = "Please select the duration of your fellowship."
        else if (a("duration") == "Other" && a("duration_other").isEmpty()) e["duration"] = "Please specify the duration."

        if (p.text("section_b", "previous_cases_count").isEmpty()) e["previous_cases_count"] = "Please select how many laparoscopic cholecystectomies you had performed."
        if (p.text("section_b", "previous_experience_level").isEmpty()) e["previous_experience_level"] = "Please select your previous level of experience."
        val reasons = p.list("section_b", "joining_reasons")
        if (reasons.isEmpty()) e["joining_reasons"] = "Please select at least one reason for joining the fellowship."
        else if ("Other" in reasons && p.text("section_b", "joining_reasons_other").isBlank()) e["joining_reasons"] = "Please specify your other reason."

        FeedbackSchema.SECTION_C.forEach { (k, l) -> if ((p.number("section_c", "ratings", k) ?: 0) == 0) e["rating_$k"] = "Please rate \"$l\" (1–5)." }
        FeedbackSchema.CASE_ROWS.forEach { (k, _) -> if (p.text("section_c", "case_counts", k).isEmpty()) e["case_count_$k"] = "Please enter a case count (0 or more)." }
        FeedbackSchema.WORKSHOPS.forEach { (k, _) -> if (p.text("section_c", "workshops", k).isEmpty()) e["workshop_$k"] = "Please select Yes or No." }
        if (p.text("section_c", "constructive_feedback_frequency").isEmpty()) e["constructive_feedback_frequency"] = "Please select constructive feedback frequency."
        if (p.text("section_c", "most_valuable_aspect").isBlank()) e["most_valuable_aspect"] = "Please share the most valuable aspect of your fellowship."
        if (p.text("section_c", "aspects_to_improve").isBlank()) e["aspects_to_improve"] = "Please share aspects that require improvement."

        FeedbackSchema.SKILLS.forEach { (k, _) ->
            if ((p.number("section_d", "skills", k, "before") ?: 0) == 0 || (p.number("section_d", "skills", k, "after") ?: 0) == 0)
                e["skill_$k"] = "Please rate both BEFORE and AFTER confidence (1–5)."
        }
        if (p.text("section_d", "overall_skill_improvement").isEmpty()) e["overall_skill_improvement"] = "Please select overall skill improvement."
        if (p.text("section_d", "confident_straightforward").isEmpty()) e["confident_straightforward"] = "Please answer Question 19."
        if (p.text("section_d", "confident_difficult").isEmpty()) e["confident_difficult"] = "Please answer Question 20."
        return e
    }

    private fun part2Errors(p: JsonObject): Map<String, String> {
        val e = linkedMapOf<String, String>()
        fun x(k: String) = p.text("section_e", k).trim()
        if (x("started_independent").isEmpty()) e["started_independent"] = "Please answer Question 21."
        if (x("started_independent") == "Yes") {
            if (x("practice_location").isEmpty()) e["practice_location"] = "Please indicate where you are performing them."
            else if (x("practice_location") == "Other" && x("practice_location_other").isEmpty()) e["practice_location"] = "Please specify the location."
        }
        if (x("independent_cases_count").isEmpty()) e["independent_cases_count"] = "Please select independent cases performed."
        if (x("regular_part_of_practice").isEmpty()) e["regular_part_of_practice"] = "Please answer Question 24."
        if (x("institution_service_impact").isEmpty()) e["institution_service_impact"] = "Please answer Question 25."
        if (x("private_practice_impact").isEmpty()) e["private_practice_impact"] = "Please answer Question 26."
        val barriers = p.list("section_e", "barriers")
        if (x("started_independent") == "No" && barriers.isEmpty()) e["barriers"] = "Please select at least one barrier."
        else if ("Other" in barriers && x("barriers_other").isEmpty()) e["barriers"] = "Please specify the other barrier."
        if (x("changed_clinical_decision_making").isEmpty()) e["changed_clinical_decision_making"] = "Please answer Question 28."
        if (p.text("section_f", "would_recommend").isEmpty()) e["would_recommend"] = "Please answer Question 29."
        if (p.text("section_f", "duration_adequate").isEmpty()) e["duration_adequate"] = "Please answer Question 30."
        if (p.text("section_f", "additional_training_recommended").isBlank()) e["additional_training_recommended"] = "Please enter your recommendation."
        if (p.text("section_f", "most_important_change").isBlank()) e["most_important_change"] = "Please share the single most important change required."
        if (p.text("section_g", "helped_professionally_by").isBlank()) e["helped_professionally_by"] = "Please complete Question 33."
        return e
    }

    fun submitPart1() {
        val s = _state.value
        if (s.submitting) return
        val errs = part1Errors(s.part1)
        if (errs.isNotEmpty()) { _state.update { it.copy(errors = errs, errorTick = it.errorTick + 1, serverError = null) }; return }
        _state.update { it.copy(errors = emptyMap(), submitting = true, serverError = null) }
        viewModelScope.launch {
            runCatching { repository.submitFeedbackPart1(s.part1) }
                .onSuccess { res ->
                    apply(res, initial = false)
                    val st = _state.value.status
                    _state.update {
                        it.copy(
                            submitting = false,
                            success = FeedbackSuccess(
                                "Part 1 feedback submitted",
                                "Your Immediate Post-Fellowship Feedback (Sections A–D) has been recorded successfully.",
                                if (st.part2CanFill) "Part 2 (Sections E–G) is unlocked and ready for you to complete."
                                else "Part 2 (6-Month Post-Fellowship Impact Questionnaire) unlocks on ${st.part2UnlockDate?.let(::prettyDate) ?: "6 months after completion"} (or earlier if unlocked by admin)."
                            )
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(submitting = false, serverError = e.userMessage("Request failed")) } }
        }
    }

    fun submitPart2() {
        val s = _state.value
        if (s.submitting) return
        val errs = part2Errors(s.part2)
        if (errs.isNotEmpty()) { _state.update { it.copy(errors = errs, errorTick = it.errorTick + 1, serverError = null) }; return }
        _state.update { it.copy(errors = emptyMap(), submitting = true, serverError = null) }
        viewModelScope.launch {
            runCatching { repository.submitFeedbackPart2(s.part2) }
                .onSuccess { res ->
                    apply(res, initial = false)
                    _state.update {
                        it.copy(
                            submitting = false,
                            success = FeedbackSuccess(
                                "Feedback questionnaire completed",
                                "Thank you! Both Part 1 and Part 2 of your Fellow Feedback & Post-Fellowship Impact Questionnaire are now complete.",
                                "Your fellowship feedback requirement is fulfilled. You are now eligible for Fellowship Certificate issuance by the administration."
                            )
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(submitting = false, serverError = e.userMessage("Request failed")) } }
        }
    }
}
