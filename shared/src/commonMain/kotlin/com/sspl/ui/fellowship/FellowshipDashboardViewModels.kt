package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.auth.RoleUtils
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.session.UserSession
import com.sspl.ui.portal.PickedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Lists + stats shown in FellowshipClinicalView.jsx (fellow and trainer variants). */
data class ClinicalData(
    val stats: ClinicalStats? = null,
    val cases: List<CaseItem> = emptyList(),
    val attendance: List<AttendanceItem> = emptyList(),
    val assessments: List<AssessmentItem> = emptyList(),
    val research: List<ResearchItem> = emptyList(),
    val fellows: List<TrainerFellow> = emptyList()
)

data class Flash(val text: String, val isError: Boolean)

/** Shared loaders/actions for the clinical workspace. List failures fall back to empty (same as web `.catch(() => ({records: []}))`). */
private suspend fun loadClinical(repo: FellowshipRepository, isTrainer: Boolean): ClinicalData = coroutineScope {
    val stats = async { runCatching { parseClinicalStats(repo.clinicalStats().let { (it["data"] as? JsonObject) ?: it }) }.getOrNull() }
    val cases = async { runCatching { repo.cases().objects().mapNotNull(::parseCase) }.getOrDefault(emptyList()) }
    val att = async { runCatching { repo.attendance().objects().mapNotNull(::parseAttendance) }.getOrDefault(emptyList()) }
    val assess = async { runCatching { repo.assessments().objects().mapNotNull(::parseAssessment) }.getOrDefault(emptyList()) }
    val research = async { runCatching { repo.research().objects().mapNotNull(::parseResearch) }.getOrDefault(emptyList()) }
    val fellows = async { if (isTrainer) runCatching { repo.trainerFellows().objects().mapNotNull(::parseTrainerFellow) }.getOrDefault(emptyList()) else emptyList() }
    ClinicalData(stats.await(), cases.await(), att.await(), assess.await(), research.await(), fellows.await())
}

// =====================================================================================
// Fellow
// =====================================================================================

sealed interface FellowDashboardContent {
    data object Loading : FellowDashboardContent
    data class Error(val message: String) : FellowDashboardContent
    /** Trainer (incl. trainer + fellow): the web sends these users to the trainer portal → open the trainer desk. */
    data object RedirectToTrainer : FellowDashboardContent
    /** Not a fellow, or a fellow without an active enrollment. */
    data class NotEnrolled(val message: String, val isTrainer: Boolean) : FellowDashboardContent
    data class Ready(
        val enrollmentId: Long?,
        val programName: String?,
        val procedures: List<String>,
        val centerName: String?,
        val trainerName: String?,
        val category: String?,
        val status: String?,
        val startedAt: String?,
        val certificate: CertificateInfo?,
        val progress: CompletionProgress?,
        val clinical: ClinicalData
    ) : FellowDashboardContent
}

data class FellowDashboardUiState(
    val content: FellowDashboardContent = FellowDashboardContent.Loading,
    val isRefreshing: Boolean = false,
    val busy: Boolean = false,
    val flash: Flash? = null
)

class FellowDashboardViewModel(
    private val repository: FellowshipRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(FellowDashboardUiState())
    val state: StateFlow<FellowDashboardUiState> = _state.asStateFlow()

    init { load(false) }

    fun refresh() = load(true)
    fun retry() = load(false)
    fun clearFlash() = _state.update { it.copy(flash = null) }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            val user = userSession.userFlow.value
            val isTrainer = RoleUtils.hasUserRole(user, "FELLOWSHIP_TRAINER")
            val isFellow = RoleUtils.hasUserRole(user, "FELLOWSHIP_FELLOW")
            if (isTrainer) {
                // FellowshipDashboardView.jsx: the trainer check wins over the fellow role.
                _state.update { it.copy(isRefreshing = false, content = FellowDashboardContent.RedirectToTrainer) }
                return@launch
            }
            if (!isFellow) {
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        content = FellowDashboardContent.NotEnrolled(
                            "Fellowship dashboard is available after admin enrolls you as a trainer or fellow.", isTrainer
                        )
                    )
                }
                return@launch
            }
            _state.update { if (refresh) it.copy(isRefreshing = true) else it.copy(content = FellowDashboardContent.Loading) }
            val result = runCatching {
                val dash = repository.fellowDashboard()
                val enrollment = dash.obj("enrollment")
                if (enrollment == null) {
                    FellowDashboardContent.NotEnrolled(dash.str("message") ?: "No active fellowship enrollment.", isTrainer)
                } else coroutineScope {
                    val enrollmentId = enrollment.long("id")
                    val clinical = async { loadClinical(repository, false) }
                    val progress = async { enrollmentId?.let { id -> runCatching { parseProgress(repository.enrollmentProgress(id).let { (it["data"] as? JsonObject) ?: it }) }.getOrNull() } }
                    val certFromList = async {
                        runCatching {
                            val list = repository.myCertificates().objects()
                            parseCertificate(list.firstOrNull { it.long("enrollment_id") == enrollmentId } ?: list.firstOrNull())
                        }.getOrNull()
                    }
                    val program = enrollment.obj("fellow_application").obj("program")
                    val center = enrollment.obj("training_center")
                    val trainer = center.obj("trainer")
                    withContext(Dispatchers.Default) {
                        FellowDashboardContent.Ready(
                            enrollmentId = enrollmentId,
                            programName = program.str("name"),
                            procedures = program.arr("procedures").names(),
                            centerName = center.str("institution_name"),
                            trainerName = listOfNotNull(trainer.str("first_name"), trainer.str("last_name")).joinToString(" ").ifBlank { center.str("master_trainer_name") },
                            category = enrollment.str("category_assigned")?.replace("CATEGORY_", "Category "),
                            status = enrollment.str("status"),
                            startedAt = enrollment.str("started_at"),
                            certificate = parseCertificate(dash.obj("certificate")) ?: parseCertificate(enrollment.obj("certificate")) ?: certFromList.await(),
                            progress = progress.await(),
                            clinical = clinical.await()
                        )
                    }
                }
            }
            _state.update { st ->
                result.fold(
                    { st.copy(content = it, isRefreshing = false) },
                    { e ->
                        if (refresh && st.content is FellowDashboardContent.Ready) st.copy(isRefreshing = false, flash = Flash(e.userMessage("Couldn't refresh."), true))
                        else st.copy(content = FellowDashboardContent.Error(e.userMessage("Failed to load dashboard")), isRefreshing = false)
                    }
                )
            }
        }
    }

    private fun act(success: String, block: suspend () -> Unit, onDone: () -> Unit = {}) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, flash = null) }
            runCatching { block() }
                .onSuccess { _state.update { it.copy(busy = false, flash = Flash(success, false)) }; onDone(); load(true) }
                .onFailure { e -> _state.update { it.copy(busy = false, flash = Flash(e.userMessage("Request failed"), true)) } }
        }
    }

    fun createCase(form: Map<String, String>, onDone: () -> Unit) = act("Case submitted for trainer review.", {
        repository.createCase(buildJsonObject { form.forEach { (k, v) -> put(k, v) } })
    }, onDone)

    fun createAttendance(form: Map<String, String>, onDone: () -> Unit) = act("Attendance recorded.", {
        repository.createAttendance(buildJsonObject { form.forEach { (k, v) -> put(k, v) } })
    }, onDone)

    fun submitResearch(form: Map<String, String>, file: PickedFile?, onDone: () -> Unit) = act("Research/publication submitted successfully.", {
        val bytes = file?.bytes
        repository.submitResearch(form, bytes, file?.name, file?.let { mimeFor(it.name, it.mimeType) } ?: "application/octet-stream")
    }, onDone)
}

// =====================================================================================
// Trainer
// =====================================================================================

sealed interface TrainerDeskContent {
    data object Loading : TrainerDeskContent
    data class Error(val message: String) : TrainerDeskContent
    data object NotTrainer : TrainerDeskContent
    data class Ready(
        val centerName: String?,
        val masterTrainer: String?,
        val programName: String?,
        val maxFellows: Int?,
        val fellowsCount: Int,
        val message: String?,
        val dashboardFellows: List<TrainerFellow>,
        val clinical: ClinicalData
    ) : TrainerDeskContent
}

data class TrainerDeskUiState(
    val content: TrainerDeskContent = TrainerDeskContent.Loading,
    val isRefreshing: Boolean = false,
    val busy: Boolean = false,
    val flash: Flash? = null
)

class TrainerDashboardViewModel(
    private val repository: FellowshipRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(TrainerDeskUiState())
    val state: StateFlow<TrainerDeskUiState> = _state.asStateFlow()

    val portalUrl: String get() = RoleUtils.getTrainerPortalRedirectUrl(userSession.token)

    init { load(false) }

    fun refresh() = load(true)
    fun retry() = load(false)
    fun clearFlash() = _state.update { it.copy(flash = null) }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            if (!RoleUtils.hasUserRole(userSession.userFlow.value, "FELLOWSHIP_TRAINER")) {
                _state.update { it.copy(content = TrainerDeskContent.NotTrainer, isRefreshing = false) }
                return@launch
            }
            _state.update { if (refresh) it.copy(isRefreshing = true) else it.copy(content = TrainerDeskContent.Loading) }
            val result = runCatching {
                coroutineScope {
                    val dash = async { repository.trainerDashboard() }
                    val clinical = async { loadClinical(repository, true) }
                    val d = dash.await()
                    val center = d.obj("center")
                    val program = center.obj("application").obj("program")
                    val fellows = d.arr("fellows").objects().mapNotNull(::parseTrainerFellow)
                    TrainerDeskContent.Ready(
                        centerName = center.str("institution_name"),
                        masterTrainer = center.str("master_trainer_name"),
                        programName = program.str("name"),
                        maxFellows = center.int("max_fellows"),
                        fellowsCount = d.int("fellows_count") ?: fellows.size,
                        message = d.str("message"),
                        dashboardFellows = fellows,
                        clinical = clinical.await()
                    )
                }
            }
            _state.update { st ->
                result.fold(
                    { st.copy(content = it, isRefreshing = false) },
                    { e ->
                        if (refresh && st.content is TrainerDeskContent.Ready) st.copy(isRefreshing = false, flash = Flash(e.userMessage("Couldn't refresh."), true))
                        else st.copy(content = TrainerDeskContent.Error(e.userMessage("Failed to load dashboard")), isRefreshing = false)
                    }
                )
            }
        }
    }

    private fun act(success: String, block: suspend () -> Unit, onDone: () -> Unit = {}) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, flash = null) }
            runCatching { block() }
                .onSuccess { _state.update { it.copy(busy = false, flash = Flash(success, false)) }; onDone(); load(true) }
                .onFailure { e -> _state.update { it.copy(busy = false, flash = Flash(e.userMessage("Request failed"), true)) } }
        }
    }

    fun acceptCase(id: Long) = act("Case accepted.", { repository.reviewCase(id, buildJsonObject { put("status", "ACCEPTED") }) })
    fun rejectCase(id: Long, feedback: String) = act("Case returned for revision.", {
        repository.reviewCase(id, buildJsonObject { put("status", "REJECTED"); put("trainer_feedback", feedback.ifBlank { "Needs revision" }) })
    })
    fun verifyAttendance(id: Long) = act("Attendance verified.", { repository.verifyAttendance(id, buildJsonObject { put("status", "ACCEPTED") }) })
    fun acceptResearch(id: Long) = act("Research accepted.", { repository.reviewResearch(id, buildJsonObject { put("status", "ACCEPTED") }) })
    fun rejectResearch(id: Long, feedback: String) = act("Research returned with feedback.", {
        repository.reviewResearch(id, buildJsonObject { put("status", "REJECTED"); put("reviewer_feedback", feedback) })
    })
    fun createAssessment(form: Map<String, String>, onDone: () -> Unit) = act("Assessment saved.", {
        repository.createAssessment(buildJsonObject { form.forEach { (k, v) -> put(k, v) } })
    }, onDone)
    fun submitFinalEvaluation(enrollmentId: String, recommendation: String, summary: String, onDone: () -> Unit) =
        act("Final evaluation submitted to admin.", {
            repository.submitFinalEvaluation(buildJsonObject {
                put("enrollment_id", enrollmentId)
                put("recommendation", recommendation)
                put("competency_met", true)
                put("overall_score", "7")
                put("trainer_summary", summary)
            })
        }, onDone)
}
