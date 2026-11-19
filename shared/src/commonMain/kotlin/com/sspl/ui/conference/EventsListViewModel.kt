package com.sspl.ui.conference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.EventItem
import com.sspl.core.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class EventsUiState(
    val isLoading: Boolean = true,
    val events: List<EventItem> = emptyList(),
    val errorMessage: String? = null,
    val selectedStatusTab: String = "upcoming", // "ongoing" | "upcoming" | "past"
    val selectedCategory: String = "ALL", // "ALL" | "CONFERENCE" | "WORKSHOP" | "MONTHLY_MEETING" | "SEMINAR"
    val searchQuery: String = ""
) {
    val ongoingCount: Int get() = events.count { it.computedStatus == "ongoing" }
    val upcomingCount: Int get() = events.count { it.computedStatus == "upcoming" }
    val pastCount: Int get() = events.count { it.computedStatus == "past" }

    val filteredEvents: List<EventItem>
        get() {
            return events.filter { item ->
                // Status match
                val matchesStatus = item.computedStatus == selectedStatusTab

                // Category match
                val matchesCategory = if (selectedCategory == "ALL") {
                    true
                } else {
                    val itemType = (item.type ?: "").uppercase()
                    when (selectedCategory) {
                        "CONFERENCE" -> itemType.contains("CONFERENCE") || itemType.contains("PROGRAM")
                        "WORKSHOP" -> itemType.contains("WORKSHOP")
                        "MONTHLY_MEETING" -> itemType.contains("MONTHLY") || itemType.contains("MEETING")
                        "SEMINAR" -> itemType.contains("SEMINAR") || itemType.contains("SYMPOSIUM") || itemType.contains("CME")
                        else -> itemType == selectedCategory
                    }
                }

                // Search query match
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val query = searchQuery.trim().lowercase()
                    item.title.lowercase().contains(query) ||
                            item.effectiveVenue.lowercase().contains(query) ||
                            item.effectiveDescription.lowercase().contains(query)
                }

                matchesStatus && matchesCategory && matchesSearch
            }
        }
}

class EventsListViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents(category: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            eventRepository.getEvents(active = null).collectLatest { list ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = list,
                    errorMessage = if (list.isEmpty()) null else null
                )
            }
        }
    }

    fun setStatusTab(tab: String) {
        _uiState.value = _uiState.value.copy(selectedStatusTab = tab)
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

