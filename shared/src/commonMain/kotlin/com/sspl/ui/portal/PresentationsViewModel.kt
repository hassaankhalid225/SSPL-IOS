package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.PortalPresentation
import com.sspl.core.repositories.PortalRepository
import com.sspl.core.repositories.PortalSubmission
import com.sspl.core.repositories.PortalUpload
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Same accept list as the web file input. */
val PRESENTATION_MIME_TYPES = listOf(
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.oasis.opendocument.presentation",
    "application/vnd.oasis.opendocument.text"
)

data class PresentationsData(
    val accepted: List<PortalSubmission>,
    val presentations: List<PortalPresentation>
)

data class PresentationsUiState(
    val data: LoadState<PresentationsData> = LoadState.Loading,
    val isRefreshing: Boolean = false,
    val selectedAbstractId: Long? = null,
    val title: String = "",
    val file: PickedFile? = null,
    val errors: Map<String, String> = emptyMap(),
    val isUploading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    /** Presentations list failed while abstracts loaded (non-blocking). */
    val listError: String? = null
) {
    val selectedAbstract get() = (data as? LoadState.Ready)?.data?.accepted?.firstOrNull { it.item.id == selectedAbstractId }
}

class PresentationsViewModel(
    private val repository: PortalRepository,
    private val preselectAbstractId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(PresentationsUiState(selectedAbstractId = preselectAbstractId))
    val state: StateFlow<PresentationsUiState> = _state.asStateFlow()

    init { load(true) }

    fun retry() = load(true)
    fun refresh() = load(false)

    private fun load(initial: Boolean) {
        _state.update { if (initial) it.copy(data = LoadState.Loading) else it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val absD = async { repository.getMySubmissions(page = 1, limit = 100) }
            val presD = async { repository.getMyPresentations() }
            val abs = absD.await()
            val pres = presD.await()
            if (abs.isFailure) {
                _state.update { s ->
                    if (!initial && s.data is LoadState.Ready) s.copy(isRefreshing = false)
                    else s.copy(data = LoadState.Error(abs.exceptionOrNull()?.message ?: "Failed to load your abstracts"), isRefreshing = false)
                }
                return@launch
            }
            val accepted = abs.getOrThrow().records.filter { it.item.status.uppercase() in setOf("ACCEPTED", "APPROVED") }
            _state.update { s ->
                val keep = s.selectedAbstractId?.takeIf { id -> accepted.any { it.item.id == id } }
                val sel = keep ?: accepted.singleOrNull()?.item?.id
                val selItem = accepted.firstOrNull { it.item.id == sel }
                s.copy(
                    data = LoadState.Ready(PresentationsData(accepted, pres.getOrNull() ?: (s.data as? LoadState.Ready)?.data?.presentations.orEmpty())),
                    listError = pres.exceptionOrNull()?.message,
                    isRefreshing = false,
                    selectedAbstractId = sel,
                    title = if (s.title.isBlank() && selItem != null) "Presentation: ${selItem.item.title}" else s.title
                )
            }
        }
    }

    fun selectAbstract(id: Long) {
        val abs = (_state.value.data as? LoadState.Ready)?.data?.accepted?.firstOrNull { it.item.id == id } ?: return
        _state.update {
            it.copy(selectedAbstractId = id, title = "Presentation: ${abs.item.title}", errors = it.errors - "abstract", errorMessage = null)
        }
    }

    fun setTitle(v: String) = _state.update { it.copy(title = v, errors = it.errors - "title", errorMessage = null) }

    fun onFilePicked(f: PickedFile?) {
        if (f == null) return
        if (f.bytes == null) {
            _state.update { it.copy(file = null, errors = it.errors + ("file" to "File size must be 100 MB or less.")) }
            return
        }
        _state.update { it.copy(file = f, errors = it.errors - "file", errorMessage = null) }
    }

    fun clearFile() = _state.update { it.copy(file = null) }
    fun dismissSuccess() = _state.update { it.copy(successMessage = null) }

    fun upload() {
        val s = _state.value
        if (s.isUploading) return
        val e = mutableMapOf<String, String>()
        if (s.selectedAbstractId == null) e["abstract"] = "Please select an accepted abstract."
        if (s.title.isBlank()) e["title"] = "Presentation title is required"
        if (s.file?.bytes == null) e["file"] = "Please attach a presentation file (PPT, PPTX, PDF, DOC, DOCX)."
        if (e.isNotEmpty()) {
            _state.update { it.copy(errors = e, errorMessage = e.values.first(), successMessage = null) }
            return
        }
        val file = s.file!!
        _state.update { it.copy(isUploading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            repository.uploadPresentation(s.selectedAbstractId!!, s.title.trim(), PortalUpload(file.bytes!!, file.name, file.mimeType))
                .onSuccess { msg ->
                    _state.update {
                        it.copy(isUploading = false, successMessage = msg, selectedAbstractId = null, title = "", file = null, errors = emptyMap())
                    }
                    load(false)
                }
                .onFailure { err ->
                    _state.update { it.copy(isUploading = false, errorMessage = err.message ?: "An error occurred during upload.") }
                }
        }
    }
}
