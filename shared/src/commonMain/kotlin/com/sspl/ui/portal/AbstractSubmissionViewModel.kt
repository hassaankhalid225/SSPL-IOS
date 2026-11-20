package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.PortalEventOption
import com.sspl.core.repositories.PortalRepository
import com.sspl.core.repositories.PortalUpload
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val LARGE_FILE_MESSAGE =
    "File size must be 100 MB or less. For larger files, please upload them to Google Drive or WeTransfer and share access with societyofsurgeons1@gmail.com."

data class AbstractForm(
    val eventId: Long? = null,
    val title: String = "",
    val authors: String = "",
    val affiliations: String = "",
    val file: PickedFile? = null
)

data class AbstractSubmissionUiState(
    val events: LoadState<List<PortalEventOption>> = LoadState.Loading,
    val form: AbstractForm = AbstractForm(),
    val errors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedEvent get() = (events as? LoadState.Ready)?.data?.firstOrNull { it.id == form.eventId }
    /** Simple 4-step progress for the indicator. */
    val completedStep: Int
        get() = when {
            form.eventId == null -> 1
            form.title.isBlank() || form.authors.isBlank() || form.affiliations.isBlank() -> 2
            form.file == null -> 3
            else -> 4
        }
}

object AbstractField {
    const val EVENT = "event"
    const val TITLE = "title"
    const val AUTHORS = "authors"
    const val AFFILIATIONS = "affiliations"
    const val FILE = "file"
}

class AbstractSubmissionViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _state = MutableStateFlow(AbstractSubmissionUiState())
    val state: StateFlow<AbstractSubmissionUiState> = _state.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        _state.update { it.copy(events = LoadState.Loading) }
        viewModelScope.launch {
            repository.getAbstractEligibleEvents()
                .onSuccess { list -> _state.update { it.copy(events = LoadState.Ready(list)) } }
                .onFailure { e -> _state.update { it.copy(events = LoadState.Error(e.message ?: "Failed to load events")) } }
        }
    }

    private fun edit(key: String, f: (AbstractForm) -> AbstractForm) =
        _state.update { it.copy(form = f(it.form), errors = it.errors - key, errorMessage = null) }

    fun selectEvent(id: Long) = edit(AbstractField.EVENT) { it.copy(eventId = id) }
    fun setTitle(v: String) = edit(AbstractField.TITLE) { it.copy(title = v) }
    fun setAuthors(v: String) = edit(AbstractField.AUTHORS) { it.copy(authors = v) }
    fun setAffiliations(v: String) = edit(AbstractField.AFFILIATIONS) { it.copy(affiliations = v) }

    fun onFilePicked(file: PickedFile?) {
        if (file == null) return
        if (file.bytes == null) {
            _state.update {
                it.copy(form = it.form.copy(file = null), errors = it.errors + (AbstractField.FILE to LARGE_FILE_MESSAGE))
            }
            return
        }
        edit(AbstractField.FILE) { it.copy(file = file) }
    }

    fun clearFile() = edit(AbstractField.FILE) { it.copy(file = null) }
    fun dismissSuccess() = _state.update { it.copy(successMessage = null) }

    fun submit() {
        val s = _state.value
        if (s.isSubmitting) return
        val f = s.form
        val e = mutableMapOf<String, String>()
        if (f.eventId == null) e[AbstractField.EVENT] = "Please select an event or monthly meeting."
        if (f.title.isBlank()) e[AbstractField.TITLE] = "Abstract title is required"
        if (f.authors.isBlank()) e[AbstractField.AUTHORS] = "Authors are required"
        if (f.affiliations.isBlank()) e[AbstractField.AFFILIATIONS] = "Affiliations are required"
        if (f.file?.bytes == null) e[AbstractField.FILE] = "Please attach a media file."
        if (e.isNotEmpty()) {
            _state.update { it.copy(errors = e, errorMessage = e.values.first(), successMessage = null) }
            return
        }
        val file = f.file!!
        _state.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            repository.submitAbstract(
                title = f.title.trim(),
                authors = f.authors.trim(),
                affiliations = f.affiliations.trim(),
                conferenceId = f.eventId,
                meetingId = null,
                attachment = PortalUpload(file.bytes!!, file.name, file.mimeType)
            ).onSuccess { msg ->
                _state.update {
                    it.copy(isSubmitting = false, form = AbstractForm(), errors = emptyMap(), successMessage = msg)
                }
            }.onFailure { err ->
                _state.update { it.copy(isSubmitting = false, errorMessage = err.message ?: "An error occurred during submission.") }
            }
        }
    }
}
