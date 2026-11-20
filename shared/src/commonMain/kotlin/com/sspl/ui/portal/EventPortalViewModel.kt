package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.PortalRegistration
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.utils.DisplayDates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EventPortalData(
    val upcoming: List<PortalRegistration>,
    val past: List<PortalRegistration>,
    /** Registrations not yet confirmed (pending approval / payment). */
    val pendingCount: Int
) {
    val isEmpty get() = upcoming.isEmpty() && past.isEmpty()
}

data class EventPortalUiState(
    val data: LoadState<EventPortalData> = LoadState.Loading,
    val tab: Int = 0,
    val isRefreshing: Boolean = false
)

class EventPortalViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _state = MutableStateFlow(EventPortalUiState())
    val state: StateFlow<EventPortalUiState> = _state.asStateFlow()

    init { load(true) }

    fun retry() = load(true)
    fun refresh() = load(false)
    fun selectTab(i: Int) = _state.update { it.copy(tab = i) }

    private fun load(initial: Boolean) {
        _state.update { if (initial) it.copy(data = LoadState.Loading) else it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.getMyRegistrations()
                .onSuccess { all ->
                    val data = withContext(Dispatchers.Default) {
                        val today = DisplayDates.today()
                        val confirmed = all.filter { it.isConfirmed }
                        val (up, past) = confirmed.partition { r ->
                            DisplayDates.parse(r.endDate ?: r.startDate)?.let { it >= today } ?: true
                        }
                        EventPortalData(
                            upcoming = up.sortedBy { it.startDate ?: "" },
                            past = past.sortedByDescending { it.startDate ?: "" },
                            pendingCount = all.size - confirmed.size
                        )
                    }
                    _state.update { s ->
                        s.copy(
                            data = LoadState.Ready(data),
                            isRefreshing = false,
                            tab = if (initial && data.upcoming.isEmpty() && data.past.isNotEmpty()) 1 else s.tab
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { s ->
                        if (!initial && s.data is LoadState.Ready) s.copy(isRefreshing = false)
                        else s.copy(data = LoadState.Error(e.message ?: "Failed to load your registered events."), isRefreshing = false)
                    }
                }
        }
    }
}
