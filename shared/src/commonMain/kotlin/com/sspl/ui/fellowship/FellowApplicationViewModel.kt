package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.session.UserSession
import com.sspl.ui.portal.PickedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class DocUpload(
    val fileName: String,
    val size: Int = 0,
    val uploading: Boolean = false,
    val url: String? = null,
    val error: String? = null
)

data class FellowReceipt(val id: String, val program: String, val status: String, val submittedAt: String? = null)

data class FellowApplicationUiState(
    val programs: LoadState<List<Program>> = LoadState.Loading,
    val program: Program? = null,
    /** No programme is open → default Basic Laparoscopy outline in read-only preview (web `closed`). */
    val closed: Boolean = false,
    val centers: List<ActiveCenter> = emptyList(),
    val stepIndex: Int = 0,
    val showErrors: Boolean = false,
    val personal: Map<String, String> = emptyMap(),
    val training: Map<String, String> = emptyMap(),
    val categoryCode: String = "",
    val answers: Map<String, String> = emptyMap(),
    /** preference id → String, or List<String> for checkbox. */
    val preferences: Map<String, Any> = emptyMap(),
    val center1: String = "",
    val center2: String = "",
    val center3: String = "",
    val files: Map<String, DocUpload> = emptyMap(),
    val agreed: Boolean = false,
    val signature: String = "",
    val signedOn: String = todayIso(),
    val submitting: Boolean = false,
    val submitError: String? = null,
    val receipt: FellowReceipt? = null
) {
    val steps: List<Pair<String, String>> get() = program?.applicationSteps().orEmpty()
    val step: String get() = steps.getOrNull(stepIndex)?.first ?: "personal"
    val category: CategoryDef? get() = program?.categories?.firstOrNull { it.code == categoryCode }
    val visiblePreferences: List<PreferenceDef>
        get() = program?.preferences.orEmpty().filter { it.showWhen.isNullOrBlank() || it.showWhen == categoryCode }
}

/**
 * E4: Fellow application wizard. Port of ApplicationWizard.jsx: same steps, validation and payload.
 * Draft + submitted receipt persist on the device per programme (web localStorage parity, see [FellowshipDraftStore]).
 * Works for guests: POST /fellowship/fellows and /fellowship/upload-document are public on the backend
 * (verified: a request with an arbitrary bearer token reaches validation, auth is not enforced).
 */
@OptIn(FlowPreview::class)
class FellowApplicationViewModel(
    private val repository: FellowshipRepository,
    private val userSession: UserSession,
    private val drafts: FellowshipDraftStore
) : ViewModel() {

    private val _state = MutableStateFlow(FellowApplicationUiState())
    val state: StateFlow<FellowApplicationUiState> = _state.asStateFlow()

    private val requestedProgramId = FellowshipNavArgs.programId
    private val requestedCenterId = FellowshipNavArgs.centerId

    /** Personal details pre-filled from the signed-in member (never for guests). */
    private val prefill: Map<String, String>

    /** Programme whose draft has been restored; autosave waits for it so an empty form can't overwrite the draft. */
    private var restoredFor: Long? = null
    private var restoreDone = false

    init {
        val u = userSession.userFlow.value
        prefill = if (u != null && !u.isGuest()) {
            val name = listOfNotNull(u.firstName, u.lastName).joinToString(" ").trim()
            buildMap {
                if (name.isNotBlank()) put("full_name", name)
                u.email?.let { e -> put("email", e) }
                u.phone?.let { p -> put("mobile", p) }
                u.pmdc?.let { p -> put("pmdc_number", p) }
                u.cnic?.let { c -> put("cnic", c) }
                u.designation?.let { d -> put("designation", d) }
                u.institution?.let { i -> put("place_of_work", i) }
            }
        } else emptyMap()
        _state.update { it.copy(personal = prefill) }
        if (requestedCenterId != null) _state.update { it.copy(center1 = requestedCenterId.toString()) }
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
                        // Web: nothing open → resolveProgram(null) in read-only preview.
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
        viewModelScope.launch {
            val list = runCatching { repository.activeCenters(p.id).objects().mapNotNull(::parseActiveCenter) }.getOrDefault(emptyList())
            _state.update { it.copy(centers = list) }
        }
    }

    /** Port of the web's load effect: a stored receipt wins; otherwise the saved draft is restored. */
    private suspend fun restoreDraft(p: Program) {
        val receipt = drafts.read(FellowshipDraftStore.fellowSubmittedKey(p.id))
        val receiptId = receipt.str("id")
        if (receiptId != null) {
            _state.update {
                it.copy(receipt = FellowReceipt(receiptId, receipt.str("program") ?: p.name, receipt.str("status") ?: "Under Review", receipt.str("submittedAt")))
            }
            return
        }
        val saved = drafts.read(FellowshipDraftStore.fellowDraftKey(p.id)) ?: return
        _state.update { st ->
            val lastStep = (p.applicationSteps().size - 1).coerceAtLeast(0)
            st.copy(
                personal = saved.stringMap("personal").ifEmpty { st.personal },
                training = saved.stringMap("training"),
                categoryCode = saved.str("categoryCode").orEmpty(),
                answers = saved.stringMap("answers"),
                preferences = saved.preferencesFromJson("preferences"),
                center1 = saved.str("center1") ?: requestedCenterId?.toString() ?: st.center1,
                center2 = saved.str("center2").orEmpty(),
                center3 = saved.str("center3").orEmpty(),
                agreed = saved.bool("agreed") == true,
                signature = saved.str("signature").orEmpty(),
                signedOn = saved.str("signedOn") ?: todayIso(),
                stepIndex = (saved.int("stepIndex") ?: 0).coerceIn(0, lastStep)
            )
        }
    }

    /** Debounced draft save on every change (web saves on each state change). Files are not persisted (web: re-attach). */
    private fun autosaveDrafts() {
        viewModelScope.launch {
            _state
                .map { s ->
                    val p = s.program
                    if (p == null || s.closed || s.receipt != null || s.submitting) null
                    else p.id to buildJsonObject {
                        put("personal", s.personal.toJsonObject())
                        put("training", s.training.toJsonObject())
                        put("categoryCode", s.categoryCode)
                        put("answers", s.answers.toJsonObject())
                        put("preferences", s.preferences.preferencesToJson())
                        put("center1", s.center1)
                        put("center2", s.center2)
                        put("center3", s.center3)
                        put("agreed", s.agreed)
                        put("signature", s.signature)
                        put("signedOn", s.signedOn)
                        put("stepIndex", s.stepIndex)
                    }
                }
                .distinctUntilChanged()
                .debounce(FellowshipDraftStore.SAVE_DEBOUNCE_MS)
                .collect { snapshot ->
                    val (id, draft) = snapshot ?: return@collect
                    if (!restoreDone || restoredFor != id || _state.value.receipt != null) return@collect
                    drafts.write(FellowshipDraftStore.fellowDraftKey(id), draft)
                }
        }
    }

    /** Receipt → "Start a new application": forget the stored receipt and open a blank form. */
    fun startNewApplication() {
        val p = _state.value.program ?: return
        viewModelScope.launch {
            drafts.remove(FellowshipDraftStore.fellowSubmittedKey(p.id))
            drafts.remove(FellowshipDraftStore.fellowDraftKey(p.id))
            _state.update {
                it.copy(
                    receipt = null, stepIndex = 0, showErrors = false, submitError = null,
                    personal = prefill, training = emptyMap(), categoryCode = "", answers = emptyMap(), preferences = emptyMap(),
                    center1 = requestedCenterId?.toString().orEmpty(), center2 = "", center3 = "",
                    files = emptyMap(), agreed = false, signature = "", signedOn = todayIso()
                )
            }
        }
    }

    fun clearProgram() {
        restoreDone = false
        restoredFor = null
        _state.update { it.copy(program = null, closed = false, stepIndex = 0, showErrors = false, receipt = null) }
    }

    /** Field edits are ignored in preview mode (read-only; web: "Nothing will be submitted"). */
    private inline fun edit(crossinline f: (FellowApplicationUiState) -> FellowApplicationUiState) {
        if (!_state.value.closed) _state.update { f(it) }
    }

    // ---- field updates
    fun setPersonal(key: String, v: String) = edit { it.copy(personal = it.personal + (key to v)) }
    fun setTraining(key: String, v: String) = edit { it.copy(training = it.training + (key to v)) }
    fun setCategory(code: String) = edit { it.copy(categoryCode = code) }
    fun setAnswer(id: String, v: String) = edit { it.copy(answers = it.answers + (id to v)) }
    fun setPreference(id: String, v: Any) = edit { it.copy(preferences = it.preferences + (id to v)) }
    fun togglePreference(id: String, option: String) = edit {
        @Suppress("UNCHECKED_CAST")
        val cur = (it.preferences[id] as? List<String>).orEmpty()
        it.copy(preferences = it.preferences + (id to (if (option in cur) cur - option else cur + option)))
    }
    fun setCenter(slot: Int, id: String) = edit {
        when (slot) {
            1 -> it.copy(center1 = id, center2 = if (it.center2 == id) "" else it.center2, center3 = if (it.center3 == id) "" else it.center3)
            2 -> it.copy(center2 = id, center3 = if (it.center3 == id) "" else it.center3)
            else -> it.copy(center3 = id)
        }
    }
    fun setAgreed(v: Boolean) = edit { it.copy(agreed = v) }
    fun setSignature(v: String) = edit { it.copy(signature = v) }
    fun setSignedOn(v: String) = edit { it.copy(signedOn = v) }

    // ---- validation (errorsFor in ApplicationWizard.jsx)
    private fun readable(label: String): String {
        val second = label.getOrNull(1)
        val acronym = second != null && second.isUpperCase()
        return if (acronym) label else label.replaceFirstChar { it.lowercase() }
    }
    private fun messageFor(label: String) = "Enter ${readable(label)}."

    fun errorsFor(s: FellowApplicationUiState, step: String): Map<String, String> {
        val p = s.program ?: return emptyMap()
        val e = linkedMapOf<String, String>()
        when (step) {
            "personal" -> p.personalFields.forEach { f ->
                if (!f.required) return@forEach
                val v = s.personal[f.key].orEmpty().trim()
                when {
                    v.isEmpty() -> e[f.key] = messageFor(f.label)
                    f.key == "email" && !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(v) -> e[f.key] = "Enter a valid email address."
                    f.key == "cnic" && v.count { it.isDigit() } < 5 -> e[f.key] = "Enter the CNIC number."
                }
            }
            "training" -> p.trainingFields.forEach { f ->
                if (f.required && s.training[f.key].isNullOrBlank()) e[f.key] = messageFor(f.label)
            }
            "experience" -> {
                if (s.categoryCode.isBlank()) e["category"] = "Select the category that matches your experience."
                s.category?.questions?.forEach { q -> if (q.required && s.answers[q.id].isNullOrBlank()) e[q.id] = messageFor(q.label) }
            }
            "preferences" -> {
                if (s.center1.isBlank() && s.centers.isNotEmpty()) e["center1"] = "Select your primary training center preference."
                s.visiblePreferences.forEach { f ->
                    val v = s.preferences[f.id]
                    val empty = if (v is List<*>) v.isEmpty() else (v as? String).isNullOrBlank()
                    if (f.required && empty) e[f.id] = if (f.type == "radio" || f.type == "dropdown") "Select ${readable(f.label)}." else messageFor(f.label)
                }
            }
            "documents" -> p.documents.forEach { d ->
                if (!d.required) return@forEach
                val f = s.files[d.id]
                when {
                    f == null -> e[d.id] = "Attach ${d.name}."
                    f.error != null -> e[d.id] = f.error
                    f.uploading -> e[d.id] = "Upload in progress for ${d.name}. Please wait."
                    f.url == null -> e[d.id] = "Upload not completed for ${d.name}. Please re-attach."
                }
            }
            "declaration" -> {
                if (!s.agreed) e["agreed"] = "Confirm the declaration to continue."
                if (s.signature.isBlank()) e["signature"] = "Type your full name as your signature."
                if (s.signedOn.isBlank()) e["date"] = "Enter the date."
            }
        }
        return e
    }

    /** @return true when moved forward. */
    fun next(): Boolean {
        val s = _state.value
        if (s.closed) {
            // Preview: browse the steps freely; submission is disabled.
            if (s.stepIndex >= s.steps.lastIndex) return false
            _state.update { it.copy(stepIndex = it.stepIndex + 1) }
            return true
        }
        if (errorsFor(s, s.step).isNotEmpty()) { _state.update { it.copy(showErrors = true) }; return false }
        if (s.stepIndex >= s.steps.lastIndex) { submit(); return false }
        _state.update { it.copy(showErrors = false, stepIndex = (it.stepIndex + 1).coerceAtMost(it.steps.lastIndex)) }
        return true
    }

    fun back() = _state.update { it.copy(showErrors = false, stepIndex = (it.stepIndex - 1).coerceAtLeast(0), submitError = null) }

    // ---- documents
    /** Port of `fileMatches` + `onFile` in ApplicationWizard.jsx. */
    fun uploadDocument(doc: DocumentDef, file: PickedFile) {
        if (_state.value.closed) return
        val fileName = file.name
        val size = file.size.toInt()
        val problem = fileProblem(fileName, file.size, doc)
        val bytes = file.bytes
        if (problem != null || bytes == null) {
            _state.update {
                it.copy(files = it.files + (doc.id to DocUpload(fileName, size,
                    error = problem ?: "File must be ${formatMb(doc.maxSizeMb)} MB or smaller.")))
            }
            return
        }
        _state.update { it.copy(files = it.files + (doc.id to DocUpload(fileName, size, uploading = true))) }
        viewModelScope.launch {
            runCatching { repository.uploadDocument(bytes, fileName, mimeFor(fileName, file.mimeType)) }
                .onSuccess { res ->
                    val url = res.str("url") ?: res.str("file_url")
                    _state.update {
                        it.copy(files = it.files + (doc.id to DocUpload(fileName, size, url = url,
                            error = if (url == null) "Upload not completed for ${doc.name}. Please re-attach." else null)))
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(files = it.files + (doc.id to DocUpload(fileName, size,
                            error = e.userMessage("Failed to upload document to server. Please try attaching again."))))
                    }
                }
        }
    }

    fun removeDocument(docId: String) = _state.update { it.copy(files = it.files - docId) }

    private fun formatMb(v: Double) = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

    // ---- submit (payload identical to ApplicationWizard.jsx)
    private fun submit() {
        val s = _state.value
        if (errorsFor(s, "declaration").isNotEmpty()) { _state.update { it.copy(showErrors = true) }; return }
        if (errorsFor(s, "documents").isNotEmpty()) {
            _state.update { it.copy(showErrors = true, submitError = "Please ensure all required documents are successfully uploaded to server storage before submitting.") }
            return
        }
        val p = s.program
        if (s.closed || p?.id == null) { _state.update { it.copy(submitError = "Applications are not open for this program.") }; return }

        _state.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            val payload = withContext(Dispatchers.Default) { buildPayload(s, p) }
            runCatching { repository.submitFellowApplication(payload) }
                .onSuccess { res ->
                    val id = res.long("id") ?: (res["data"] as? kotlinx.serialization.json.JsonObject).long("id") ?: 0L
                    val receipt = FellowReceipt("MF-${currentYear()}-${id.toString().padStart(5, '0')}", p.name, "Under Review", Clock.System.now().toString())
                    _state.update { it.copy(submitting = false, receipt = receipt) }
                    drafts.write(FellowshipDraftStore.fellowSubmittedKey(p.id), buildJsonObject {
                        put("id", receipt.id)
                        put("program", receipt.program)
                        put("status", receipt.status)
                        receipt.submittedAt?.let { put("submittedAt", it) }
                    })
                    drafts.remove(FellowshipDraftStore.fellowDraftKey(p.id))
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(submitting = false, submitError = e.userMessage("The application could not be submitted. Check the highlighted details and try again."))
                    }
                }
        }
    }

    private fun buildPayload(s: FellowApplicationUiState, p: Program) = buildJsonObject {
        fun pers(k: String) = s.personal[k].orEmpty().trim()
        put("program_id", p.id)
        put("full_name", pers("full_name"))
        put("father_spouse_name", pers("father_spouse_name"))
        put("cnic", pers("cnic"))
        put("pmdc_number", pers("pmdc_number"))
        put("qualifications", pers("qualifications"))
        put("designation", pers("designation"))
        put("place_of_work", pers("place_of_work"))
        put("mobile", pers("mobile"))
        put("whatsapp", pers("whatsapp"))
        put("email", pers("email"))
        put("postal_address", pers("postal_address"))
        put("training_details", buildJsonObject { s.training.forEach { (k, v) -> put(k, v) } })
        put("laparoscopic_exp", buildJsonObject {
            put("self_assessment_category", s.categoryCode)
            s.category?.questions?.forEach { q -> s.answers[q.id]?.takeIf { it.isNotBlank() }?.let { put(answerKey(q.label), it) } }
        })
        put("preferences", buildJsonObject {
            val c1 = s.centers.firstOrNull { it.id.toString() == s.center1 }
            val c2 = s.centers.firstOrNull { it.id.toString() == s.center2 }
            val c3 = s.centers.firstOrNull { it.id.toString() == s.center3 }
            if (c1 != null || s.center1.isNotBlank()) {
                put("preferred_center_id", c1?.id ?: s.center1.toLongOrNull())
                put("preferred_center_name", c1?.name ?: "")
                put("preferred_center_city", c1?.city ?: "")
            }
            if (c2 != null || s.center2.isNotBlank()) {
                put("preferred_center_id_2", c2?.id ?: s.center2.toLongOrNull())
                put("preferred_center_name_2", c2?.name ?: "")
            }
            if (c3 != null || s.center3.isNotBlank()) {
                put("preferred_center_id_3", c3?.id ?: s.center3.toLongOrNull())
                put("preferred_center_name_3", c3?.name ?: "")
            }
            s.visiblePreferences.forEach { f ->
                when (val v = s.preferences[f.id]) {
                    is String -> if (v.isNotEmpty()) put(preferenceKey(f), v)
                    is List<*> -> put(preferenceKey(f), JsonArray(v.map { JsonPrimitive(it.toString()) }))
                    else -> Unit
                }
            }
        })
        val attachedDocs = p.documents.filter { s.files[it.id] != null }
        put("declaration", buildJsonObject {
            put("agreed", true)
            put("applicant_name", s.signature)
            put("signature", s.signature)
            put("date", s.signedOn)
            put("documents_attached", attachedDocs.joinToString(", ") { "${it.name} (${s.files[it.id]!!.fileName})" })
            put("documents", buildJsonArray {
                attachedDocs.forEach { d ->
                    val f = s.files[d.id]!!
                    add(buildJsonObject {
                        put("id", d.id)
                        put("name", d.name)
                        put("fileName", f.fileName)
                        if (f.url != null) put("url", f.url) else put("url", JsonNull)
                        put("size", f.size)
                    })
                }
            })
        })
    }
}

/** `fileMatches(file, document)`: JPEG→JPG, DOCX→DOC, then allowed types and max size. */
internal fun fileProblem(fileName: String, size: Long, doc: DocumentDef): String? {
    val raw = fileName.substringAfterLast('.', "").uppercase()
    val ext = when (raw) { "JPEG" -> "JPG"; "DOCX" -> "DOC"; else -> raw }
    if (doc.fileTypes.isNotEmpty() && ext !in doc.fileTypes) return "Use ${doc.fileTypes.joinToString(", ")}."
    val max = doc.maxSizeMb.takeIf { it > 0 } ?: 5.0
    if (size > max * 1024 * 1024) {
        val m = if (max % 1.0 == 0.0) max.toInt().toString() else max.toString()
        return "File must be $m MB or smaller."
    }
    return null
}

/** Content type for the multipart part (falls back to the extension when the picker gives none). */
internal fun mimeFor(fileName: String, picked: String?): String {
    if (!picked.isNullOrBlank() && picked != "application/octet-stream") return picked
    return when (fileName.substringAfterLast('.', "").lowercase()) {
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "zip" -> "application/zip"
        else -> "application/octet-stream"
    }
}

/** Picker MIME filter for the web's file-type labels. */
internal fun mimeTypesForLabels(labels: Collection<String>): List<String> = labels.flatMap {
    when (it.uppercase()) {
        "PDF" -> listOf("application/pdf")
        "DOC", "DOCX" -> listOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        "JPG", "JPEG" -> listOf("image/jpeg")
        "PNG" -> listOf("image/png")
        "ZIP" -> listOf("application/zip")
        else -> listOf("*/*")
    }
}.distinct().ifEmpty { listOf("any") }
