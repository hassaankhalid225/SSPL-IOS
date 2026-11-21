package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.FellowshipApiException
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

val CENTER_STEPS = listOf(
    "basic" to "Centre & trainer",
    "facilities" to "Facilities",
    "cases" to "Case volume",
    "academic" to "Academic",
    "declaration" to "Declaration",
)

val DEFAULT_CENTER_FACILITIES = listOf(
    "Dedicated Operation Theatre",
    "Laparoscopic Tower",
    "HD Camera System & Monitor",
    "CO2 Insufflator",
    "Electrosurgical / Energy Device",
    "Suction / Irrigation System",
    "Laparoscopic Hand Instruments Set",
    "Endo-suturing Instruments",
    "Sterilization & Autoclave Unit",
    "Post-operative Recovery / HDU",
    "Emergency / ICU Backup Support",
)

val CENTER_PARTICIPATION = listOf(
    "Pre-operative Assessment", "OT Assistance", "Camera Handling", "Port Placement",
    "Dissection Under Supervision", "Post-operative Care", "Morbidity / Audit Meetings",
)

val CENTER_ACADEMIC_ACTIVITIES = listOf(
    "Case Discussions", "Journal Club", "Morbidity & Mortality Meetings", "Skills Workshops", "Video Review Sessions",
)

/** Result dialog content (FellowshipResultModal). */
data class CenterDialog(val tone: NoteTone, val title: String, val message: String, val subtext: String)

data class CenterReceipt(
    val id: String,
    val program: String,
    val institution: String,
    val masterTrainer: String,
    val status: String,
    val submittedAt: String? = null
)

data class CenterApplicationUiState(
    val programs: LoadState<List<Program>> = LoadState.Loading,
    val program: Program? = null,
    /** No programme is open → read-only preview of the wizard (web `closed`). */
    val closed: Boolean = false,
    val stepIndex: Int = 0,
    val showErrors: Boolean = false,
    val basic: Map<String, String> = emptyMap(),
    val facilities: List<String> = emptyList(),
    val customFacility: String = "",
    val caseVolume: Map<String, String> = emptyMap(),
    val participation: List<String> = emptyList(),
    val regularActivities: String = "",
    val activities: List<String> = emptyList(),
    val applicantName: String = "",
    val signature: String = "",
    val date: String = todayIso(),
    val stamp: String = "",
    val agreed: Boolean = false,
    val submitting: Boolean = false,
    val dialog: CenterDialog? = null,
    val receipt: CenterReceipt? = null
) {
    val step: String get() = CENTER_STEPS[stepIndex].first
    val availableFacilities: List<String>
        get() = program?.rawFacilities?.ifEmpty { program.facilities }?.ifEmpty { null } ?: DEFAULT_CENTER_FACILITIES
}

/**
 * E5: Training centre application — port of CenterApplicationWizard.jsx (steps, validation, payload).
 * Public endpoint (guests can apply). Draft + receipt persist per programme like the web's localStorage.
 */
@OptIn(FlowPreview::class)
class CenterApplicationViewModel(
    private val repository: FellowshipRepository,
    private val drafts: FellowshipDraftStore
) : ViewModel() {

    private val _state = MutableStateFlow(CenterApplicationUiState())
    val state: StateFlow<CenterApplicationUiState> = _state.asStateFlow()
    private val requestedProgramId = FellowshipNavArgs.programId

    private var restoredFor: Long? = null
    private var restoreDone = false

    init {
        loadPrograms()
        autosaveDrafts()
    }

    fun loadPrograms() {
        viewModelScope.launch {
            _state.update { it.copy(programs = LoadState.Loading) }
            runCatching { withContext(Dispatchers.Default) { repository.openPrograms().objects().map(::parseProgram) } }
                .onSuccess { list ->
                    val chosen = list.firstOrNull { it.id == requestedProgramId } ?: list.singleOrNull()
                    when {
                        chosen != null -> selectProgram(chosen)
                        // Web FellowshipTrainingCenterPage: nothing open → resolveProgram(null), closed preview.
                        list.isEmpty() -> selectProgram(defaultLaparoscopyProgram(), closed = true)
                    }
                    _state.update { it.copy(programs = LoadState.Ready(list)) }
                }
                .onFailure { e -> _state.update { it.copy(programs = LoadState.Error(e.userMessage("Fellowship programs could not be loaded."))) } }
        }
    }

    fun selectProgram(p: Program) = selectProgram(p, closed = false)

    private fun selectProgram(p: Program, closed: Boolean) {
        restoreDone = false
        _state.update { it.copy(program = p, closed = closed, stepIndex = 0, showErrors = false, receipt = null) }
        viewModelScope.launch {
            if (!closed) restoreDraft(p)
            restoredFor = p.id
            restoreDone = true
        }
    }

    fun clearProgram() {
        restoreDone = false
        restoredFor = null
        _state.update { it.copy(program = null, closed = false, stepIndex = 0, showErrors = false, receipt = null) }
    }

    private suspend fun restoreDraft(p: Program) {
        val receipt = drafts.read(FellowshipDraftStore.centerSubmittedKey(p.id))
        val receiptId = receipt.str("id")
        if (receiptId != null) {
            _state.update {
                it.copy(
                    receipt = CenterReceipt(
                        id = receiptId,
                        program = receipt.str("program") ?: p.name,
                        institution = receipt.str("institution").orEmpty(),
                        masterTrainer = receipt.str("master_trainer").orEmpty(),
                        status = receipt.str("status") ?: "Under Review",
                        submittedAt = receipt.str("submittedAt")
                    )
                )
            }
            return
        }
        val saved = drafts.read(FellowshipDraftStore.centerDraftKey(p.id)) ?: return
        val cv = saved.obj("caseVolume")
        val academic = saved.obj("academic")
        val decl = saved.obj("declaration")
        _state.update {
            it.copy(
                basic = saved.stringMap("basic"),
                facilities = saved.stringList("facilities"),
                caseVolume = cv?.filterValues { v -> v is JsonPrimitive }?.mapNotNull { (k, v) -> (v as JsonPrimitive).contentOrNull?.let { k to it } }?.toMap().orEmpty(),
                participation = cv.stringList("participation"),
                regularActivities = academic.str("regular_activities").orEmpty(),
                activities = academic.stringList("activities"),
                applicantName = decl.str("applicant_name").orEmpty(),
                signature = decl.str("signature").orEmpty(),
                date = decl.str("date") ?: todayIso(),
                stamp = decl.str("institution_stamp").orEmpty(),
                agreed = decl.bool("agreed") == true,
                stepIndex = (saved.int("stepIndex") ?: 0).coerceIn(0, CENTER_STEPS.lastIndex)
            )
        }
    }

    /** Same shape as the web draft: {basic, facilities, caseVolume{…, participation}, academic, declaration, stepIndex}. */
    private fun autosaveDrafts() {
        viewModelScope.launch {
            _state
                .map { s ->
                    val p = s.program
                    if (p == null || s.closed || s.receipt != null || s.submitting) null
                    else p.id to buildJsonObject {
                        put("basic", s.basic.toJsonObject())
                        put("facilities", s.facilities.toJsonArray())
                        put("caseVolume", buildJsonObject {
                            s.caseVolume.forEach { (k, v) -> put(k, v) }
                            put("participation", s.participation.toJsonArray())
                        })
                        put("academic", buildJsonObject {
                            put("regular_activities", s.regularActivities)
                            put("activities", s.activities.toJsonArray())
                        })
                        put("declaration", buildJsonObject {
                            put("applicant_name", s.applicantName)
                            put("signature", s.signature)
                            put("date", s.date)
                            put("institution_stamp", s.stamp)
                            put("agreed", s.agreed)
                        })
                        put("stepIndex", s.stepIndex)
                    }
                }
                .distinctUntilChanged()
                .debounce(FellowshipDraftStore.SAVE_DEBOUNCE_MS)
                .collect { snapshot ->
                    val (id, draft) = snapshot ?: return@collect
                    if (!restoreDone || restoredFor != id || _state.value.receipt != null) return@collect
                    drafts.write(FellowshipDraftStore.centerDraftKey(id), draft)
                }
        }
    }

    /** Receipt → "Start a new application". */
    fun startNewApplication() {
        val p = _state.value.program ?: return
        viewModelScope.launch {
            drafts.remove(FellowshipDraftStore.centerSubmittedKey(p.id))
            drafts.remove(FellowshipDraftStore.centerDraftKey(p.id))
            _state.update {
                CenterApplicationUiState(programs = it.programs, program = it.program, closed = it.closed)
            }
        }
    }

    /** Edits are ignored in preview mode (read-only). */
    private inline fun edit(crossinline f: (CenterApplicationUiState) -> CenterApplicationUiState) {
        if (!_state.value.closed) _state.update { f(it) }
    }

    fun setBasic(k: String, v: String) = edit { it.copy(basic = it.basic + (k to v)) }
    fun toggleFacility(f: String) = edit { it.copy(facilities = if (f in it.facilities) it.facilities - f else it.facilities + f) }
    fun setCustomFacility(v: String) = edit { it.copy(customFacility = v) }
    fun addCustomFacility() = edit {
        val t = it.customFacility.trim()
        if (t.isEmpty()) it else it.copy(facilities = if (t in it.facilities) it.facilities else it.facilities + t, customFacility = "")
    }
    fun setCase(k: String, v: String) = edit { it.copy(caseVolume = it.caseVolume + (k to v)) }
    fun toggleParticipation(v: String) = edit { it.copy(participation = if (v in it.participation) it.participation - v else it.participation + v) }
    fun setRegular(v: String) = edit { it.copy(regularActivities = v) }
    fun toggleActivity(v: String) = edit { it.copy(activities = if (v in it.activities) it.activities - v else it.activities + v) }
    fun setApplicant(v: String) = edit { it.copy(applicantName = v) }
    fun setSignature(v: String) = edit { it.copy(signature = v) }
    fun setDate(v: String) = edit { it.copy(date = v) }
    fun setStamp(v: String) = edit { it.copy(stamp = v) }
    fun setAgreed(v: Boolean) = edit { it.copy(agreed = v) }
    fun dismissDialog() = _state.update { it.copy(dialog = null) }
    fun goTo(index: Int) = _state.update { if (index < it.stepIndex) it.copy(stepIndex = index, showErrors = false) else it }

    fun errorsFor(s: CenterApplicationUiState, step: String): Map<String, String> {
        val e = linkedMapOf<String, String>()
        fun b(k: String) = s.basic[k].orEmpty().trim()
        when (step) {
            "basic" -> {
                if (b("institution_name").isEmpty()) e["institution_name"] = "Enter the name of the institution / hospital."
                if (b("master_trainer_name").isEmpty()) e["master_trainer_name"] = "Enter the name of the proposed master trainer."
                if (b("pmdc_number").isEmpty()) e["pmdc_number"] = "Enter the PMDC registration number."
                if (b("mobile").isEmpty()) e["mobile"] = "Enter a valid mobile contact number."
                if (b("email").isEmpty()) e["email"] = "Enter an email address."
                else if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(b("email"))) e["email"] = "Enter a valid email address."
            }
            "facilities" -> if (s.facilities.isEmpty()) e["facilities"] = "Please select at least one facility available at your center."
            "cases" -> if (s.caseVolume["hands_on_exposure"].isNullOrBlank()) e["hands_on_exposure"] = "Please select whether fellows will receive hands-on surgical exposure."
            "academic" -> if (s.regularActivities.isBlank()) e["regular_activities"] = "Please specify whether regular academic activities are conducted."
            "declaration" -> {
                if (!s.agreed) e["agreed"] = "You must confirm the undertaking before submitting."
                if (s.applicantName.isBlank()) e["applicant_name"] = "Enter the applicant / trainer name."
                if (s.signature.isBlank()) e["signature"] = "Type your full name as digital signature."
                if (s.date.isBlank()) e["date"] = "Enter the date."
            }
        }
        return e
    }

    fun next() {
        val s = _state.value
        if (s.closed) {
            // Preview: browse steps without validation; submit stays disabled.
            if (s.stepIndex < CENTER_STEPS.lastIndex) _state.update { it.copy(stepIndex = it.stepIndex + 1) }
            return
        }
        if (errorsFor(s, s.step).isNotEmpty()) { _state.update { it.copy(showErrors = true) }; return }
        if (s.stepIndex == CENTER_STEPS.lastIndex) submit()
        else _state.update { it.copy(showErrors = false, stepIndex = it.stepIndex + 1) }
    }

    fun back() = _state.update { it.copy(showErrors = false, stepIndex = (it.stepIndex - 1).coerceAtLeast(0)) }

    private fun submit() {
        val s = _state.value
        val p = s.program
        if (s.closed || p?.id == null) {
            _state.update {
                it.copy(dialog = CenterDialog(NoteTone.Warning, "Applications closed",
                    "Fellowship training center applications are not open for this program currently.",
                    "Please check back later or contact the Society administration for announcements."))
            }
            return
        }
        if (s.submitting) return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            val payload = buildJsonObject {
                listOf("institution_name", "unit_department", "master_trainer_name", "pmdc_number", "qualifications",
                    "designation", "years_practice", "mobile", "email", "whatsapp", "hospital_address")
                    .forEach { k -> put(k, s.basic[k].orEmpty().let { v -> if (k == "mobile" || k == "whatsapp") v.trim() else v }) }
                put("program_id", p.id)
                put("facilities", JsonArray(s.facilities.map(::JsonPrimitive)))
                put("case_volume", buildJsonObject {
                    listOf("per_week", "per_month", "last_12_months", "hands_on_exposure", "fellows_per_year")
                        .forEach { k -> put(k, s.caseVolume[k].orEmpty()) }
                    put("participation", JsonArray(s.participation.map(::JsonPrimitive)))
                })
                put("academic_env", buildJsonObject {
                    put("regular_activities", s.regularActivities)
                    put("activities", JsonArray(s.activities.map(::JsonPrimitive)))
                })
                put("declaration", buildJsonObject {
                    put("applicant_name", s.applicantName)
                    put("signature", s.signature)
                    put("date", s.date)
                    put("institution_stamp", s.stamp)
                    put("agreed", s.agreed)
                })
            }
            runCatching { repository.submitCenterApplication(payload) }
                .onSuccess { res ->
                    val id = res.long("id") ?: res.obj("data").long("id")
                    val receipt = CenterReceipt(
                        id = "TC-${currentYear()}-${(id ?: 0L).toString().padStart(5, '0')}",
                        program = p.name,
                        institution = s.basic["institution_name"].orEmpty(),
                        masterTrainer = s.basic["master_trainer_name"].orEmpty(),
                        status = "Under Review",
                        submittedAt = Clock.System.now().toString()
                    )
                    drafts.write(FellowshipDraftStore.centerSubmittedKey(p.id), buildJsonObject {
                        put("id", receipt.id)
                        put("program", receipt.program)
                        put("institution", receipt.institution)
                        put("master_trainer", receipt.masterTrainer)
                        put("status", receipt.status)
                        receipt.submittedAt?.let { put("submittedAt", it) }
                    })
                    drafts.remove(FellowshipDraftStore.centerDraftKey(p.id))
                    _state.update {
                        it.copy(
                            submitting = false, receipt = receipt,
                            dialog = CenterDialog(NoteTone.Success, "Application submitted successfully",
                                "Your training center application and undertaking have been submitted successfully. The Society of Surgeons Lahore Chapter will review your center details and contact you for verification.",
                                "All facility details and surgical case volumes will be reviewed by the Society before formal accreditation. Once approved, your center's credentials will be emailed to you.")
                        )
                    }
                }
                .onFailure { e ->
                    val msg = e.userMessage("Failed to submit training center application. Please check your details and try again.")
                    val duplicate = (e as? FellowshipApiException)?.status == 409 ||
                        msg.contains("already", true) || msg.contains("duplicate", true)
                    _state.update {
                        it.copy(
                            submitting = false,
                            dialog = if (duplicate) CenterDialog(NoteTone.Warning, "Application already submitted", msg,
                                "Please do not submit duplicate applications. You will be contacted once your center application has been reviewed.")
                            else CenterDialog(NoteTone.Error, "Submission failed", msg,
                                "Please ensure all required fields, valid email, and contact details are filled in correctly.")
                        )
                    }
                }
        }
    }
}
