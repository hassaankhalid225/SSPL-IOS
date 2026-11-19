package com.sspl.ui.conference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.EventArchiveRecord
import com.sspl.core.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val isLoading: Boolean = true,
    val archives: List<EventArchiveRecord> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val filteredArchives: List<EventArchiveRecord>
        get() {
            if (searchQuery.isBlank()) return archives
            val q = searchQuery.trim().lowercase()
            return archives.filter { archive ->
                archive.conference?.title?.lowercase()?.contains(q) == true ||
                        archive.notes?.lowercase()?.contains(q) == true ||
                        archive.materials.any { it.title?.lowercase()?.contains(q) == true }
            }
        }
}

class EventArchiveViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadArchives()
    }

    fun loadArchives() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            eventRepository.getPublicArchives().collectLatest { list ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    archives = list
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

