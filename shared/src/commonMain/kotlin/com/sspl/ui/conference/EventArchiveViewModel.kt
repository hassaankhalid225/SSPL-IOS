package com.sspl.ui.conference

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.EventArchiveRecord
import com.sspl.core.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ArchiveUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val archives: List<EventArchiveRecord> = emptyList(),
    val searchQuery: String = "",
    val visible: List<EventArchiveRecord> = emptyList(),
    val totalMaterials: Int = 0,
    val errorMessage: String? = null,
    val loaded: Boolean = false
)

/** C8: GET /archive/public, searchable locally. */
class EventArchiveViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init { loadArchives() }

    fun refresh() = loadArchives(refresh = true)

    fun loadArchives(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !it.loaded, isRefreshing = refresh && it.loaded, errorMessage = null) }
            eventRepository.loadPublicArchives()
                .onSuccess { list ->
                    val sorted = list.sortedByDescending { it.conference?.startDate ?: "" }
                    _uiState.update {
                        filter(it.copy(archives = sorted, loaded = true, isLoading = false, isRefreshing = false,
                            totalMaterials = sorted.sumOf { a -> a.materials.size }))
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false,
                            errorMessage = e.toUserMessage("The archive couldn't be loaded. Try again."))
                    }
                }
        }
    }

    fun setSearchQuery(query: String) = _uiState.update { filter(it.copy(searchQuery = query)) }

    private fun filter(s: ArchiveUiState): ArchiveUiState {
        val q = s.searchQuery.trim().lowercase()
        if (q.isEmpty()) return s.copy(visible = s.archives)
        return s.copy(visible = s.archives.filter { a ->
            a.conference?.title?.lowercase()?.contains(q) == true ||
                a.notes?.lowercase()?.contains(q) == true ||
                a.materials.any { it.title?.lowercase()?.contains(q) == true }
        })
    }
}
