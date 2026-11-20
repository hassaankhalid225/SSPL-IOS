package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.session.UserSession
import com.sspl.ui.portal.PickedFile
import kotlinx.coroutines.Dispatchers
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

data class FellowReceipt(val id: String, val program: String, val status: String)

data class FellowApplicationUiState(
    val programs: LoadState<List<Program>> = LoadState.Loading,
    val program: Program? = null,
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
 * (Draft persistence across app restarts is not implemented; the draft survives configuration changes.)
 */
class FellowApplicationViewModel(
    private val repository: FellowshipRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(FellowApplicationUiState())
    val state: StateFlow<FellowApplicationUiState> = _state.asStateFlow()

    private val requestedProgramId = FellowshipNavArgs.programId
    private val requestedCenterId = FellowshipNavArgs.centerId

    init {
        val u = userSession.userFlow.value
        if (u != null && !u.isGuest()) {
            val name = listOfNotNull(u.firstName, u.lastName).joinToString(" ").trim()
            _state.update {
                it.copy(personal = buildMap {
                    if (name.isNotBlank()) put("full_name", name)
                    u.email?.let { e -> put("email", e) }
                    u.phone?.let { p -> put("mobile", p) }
                    u.pmdc?.let { p -> put("pmdc_number", p) }
                    u.cnic?.let { c -> put("cnic", c) }
                    u.designation?.let { d -> put("designation", d) }
                    u.institution?.let { i -> put("place_of_work", i) }
                })
            }
        }
        if (requestedCenterId != null) _state.update { it.copy(center1 = requestedCenterId.toString()) }
        loadPrograms()
    }

    fun loadPrograms() {
        viewModelScope.launch {
            _state.update { it.copy(programs = LoadState.Loading) }
            runCatching { withContext(Dispatchers.Default) { repository.openPrograms().objects().map(::parseProgram) } }
                .onSuccess { list ->
                    val chosen = list.firstOrNull { it.id == requestedProgramId } ?: list.singleOrNull()
                    _state.update { it.copy(programs = LoadState.Ready(list)) }
                    if (chosen != null) selectProgram(chosen)
                }
                .onFailure { e -> _state.update { it.copy(programs = LoadState.Error(e.userMessage("Fellowship programs could not be loaded."))) } }
        }
    }

    fun selectProgram(p: Program) {
        _state.update { it.copy(program = p, stepIndex = 0, showErrors = false) }
        viewModelScope.launch {
            val list = runCatching { repository.activeCenters(p.id).objects().mapNotNull(::parseActiveCenter) }.getOrDefault(emptyList())
            _state.update { it.copy(centers = list) }
        }
    }

    fun clearProgram() = _state.update { it.copy(program = null, stepIndex = 0, showErrors = false) }

    // ---- field updates
    fun setPersonal(key: String, v: String) = _state.update { it.copy(personal = it.personal + (key to v)) }
    fun setTraining(key: String, v: String) = _state.update { it.copy(training = it.training + (key to v)) }
    fun setCategory(code: String) = _state.update { it.copy(categoryCode = code) }
    fun setAnswer(id: String, v: String) = _state.update { it.copy(answers = it.answers + (id to v)) }
    fun setPreference(id: String, v: Any) = _state.update { it.copy(preferences = it.preferences + (id to v)) }
    fun togglePreference(id: String, option: String) = _state.update {
        @Suppress("UNCHECKED_CAST")
        val cur = (it.preferences[id] as? List<String>).orEmpty()
        it.copy(preferences = it.preferences + (id to (if (option in cur) cur - option else cur + option)))
    }
    fun setCenter(slot: Int, id: String) = _state.update {
        when (slot) {
            1 -> it.copy(center1 = id, center2 = if (it.center2 == id) "" else it.center2, center3 = if (it.center3 == id) "" else it.center3)
            2 -> it.copy(center2 = id, center3 = if (it.center3 == id) "" else it.center3)
            else -> it.copy(center3 = id)
        }
    }
    fun setAgreed(v: Boolean) = _state.update { it.copy(agreed = v) }
    fun setSignature(v: String) = _state.update { it.copy(signature = v) }
    fun setSignedOn(v: String) = _state.update { it.copy(signedOn = v) }

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
        if (errorsFor(s, s.step).isNotEmpty()) { _state.update { it.copy(showErrors = true) }; return false }
        if (s.stepIndex >= s.steps.lastIndex) { submit(); return false }
        _state.update { it.copy(showErrors = false, stepIndex = (it.stepIndex + 1).coerceAtMost(it.steps.lastIndex)) }
        return true
    }

    fun back() = _state.update { it.copy(showErrors = false, stepIndex = (it.stepIndex - 1).coerceAtLeast(0), submitError = null) }

    // ---- documents
    /** Port of `fileMatches` + `onFile` in ApplicationWizard.jsx. */
    fun uploadDocument(doc: DocumentDef, file: PickedFile) {
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
        if (p?.id == null) { _state.update { it.copy(submitError = "Applications are not open for this program.") }; return }

        _state.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            val payload = withContext(Dispatchers.Default) { buildPayload(s, p) }
            runCatching { repository.submitFellowApplication(payload) }
                .onSuccess { res ->
                    val id = res.long("id") ?: (res["data"] as? kotlinx.serialization.json.JsonObject).long("id") ?: 0L
                    _state.update {
                        it.copy(
                            submitting = false,
                            receipt = FellowReceipt("MF-${currentYear()}-${id.toString().padStart(5, '0')}", p.name, "Under Review")
                        )
                    }
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
