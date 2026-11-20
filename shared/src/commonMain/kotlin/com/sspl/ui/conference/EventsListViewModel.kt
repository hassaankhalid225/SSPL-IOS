package com.sspl.ui.conference

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.EventItem
import com.sspl.core.repositories.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Events list state. Filtered list + tab counts are PRECOMPUTED here (never in getters),
 * so recomposition during scroll does no filtering or date parsing.
 */
@Immutable
data class EventsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val scope: EventScope = EventScope.ALL,
    val all: List<EventCardUi> = emptyList(),
    val tab: EventStatusTab = EventStatusTab.UPCOMING,
    val category: EventCategory = EventCategory.ALL,
    val query: String = "",
    val visible: List<EventCardUi> = emptyList(),
    val counts: List<Int> = listOf(0, 0, 0) // aligned with EventStatusTab.entries
) {
    val hasData: Boolean get() = all.isNotEmpty()
}

class EventsListViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        // Same cache key as Home (active = null) so the multi-MB download is shared.
        eventRepository.cachedEvents(active = null)?.let { cached ->
            viewModelScope.launch { applyEvents(cached, loading = true) }
        }
        load(force = false)
    }

    /** Restrict the screen to conferences / workshops (Conferences & Workshops screens). */
    fun setScope(scope: EventScope) {
        if (_uiState.value.scope == scope) return
        _uiState.update { recompute(it.copy(scope = scope, category = EventCategory.ALL)) }
    }

    fun refresh() = load(force = true)

    fun retry() = load(force = true)

    private fun load(force: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !it.hasData,
                    isRefreshing = force && it.hasData,
                    errorMessage = null
                )
            }
            eventRepository.loadEvents(active = null, forceRefresh = force)
                .onSuccess { applyEvents(it, loading = false) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            // Keep showing cached rows if we have them; only show the error
                            // screen when there is nothing to show.
                            errorMessage = e.toUserMessage("Events couldn't be loaded right now. Pull to refresh or try again.")
                        )
                    }
                }
        }
    }

    private suspend fun applyEvents(list: List<EventItem>, loading: Boolean) {
        val cards = withContext(Dispatchers.Default) {
            list.asSequence()
                .filter { it.title.isNotBlank() || it.effectiveId != 0L }
                .distinctBy { it.effectiveId }
                .map { it.toCardUi() }
                .toList()
        }
        _uiState.update {
            recompute(it.copy(all = cards, isLoading = loading && cards.isEmpty(), isRefreshing = false, errorMessage = null))
        }
    }

    fun setStatusTab(tab: EventStatusTab) {
        if (_uiState.value.tab == tab) return
        _uiState.update { recompute(it.copy(tab = tab)) }
    }

    fun setCategory(category: EventCategory) {
        if (_uiState.value.category == category) return
        _uiState.update { recompute(it.copy(category = category)) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { recompute(it.copy(query = query)) }
    }

    fun clearFilters() {
        _uiState.update { recompute(it.copy(category = EventCategory.ALL, query = "", tab = EventStatusTab.UPCOMING)) }
    }

    /** Cheap: works on precomputed strings only (no date parsing). */
    private fun recompute(s: EventsUiState): EventsUiState {
        val q = s.query.trim().lowercase()
        val base = s.all.filter { e ->
            s.scope.includes(e.type) &&
                s.category.matches(e.type) &&
                (q.isEmpty() || e.searchBlob.contains(q))
        }
        val counts = EventStatusTab.entries.map { t -> base.count { it.status == t.key } }
        val visible = base.filter { it.status == s.tab.key }.let { l ->
            if (s.tab == EventStatusTab.PAST) l.sortedByDescending { it.startKey } else l.sortedBy { it.startKey }
        }
        return s.copy(visible = visible, counts = counts)
    }
}
