package com.sspl.ui.elections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.ElectionItem
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.utils.DisplayDates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ElectionPhase(val label: String) { ONGOING("Ongoing"), UPCOMING("Upcoming"), CLOSED("Closed") }

data class ElectionUi(val item: ElectionItem, val phase: ElectionPhase)

data class ElectionsUiState(
    val elections: LoadState<List<ElectionUi>> = LoadState.Loading,
    val isRefreshing: Boolean = false,
    val query: String = "",
    /** null = all phases. */
    val phase: ElectionPhase? = null
) {
    val all: List<ElectionUi> get() = (elections as? LoadState.Ready)?.data.orEmpty()
}

/** Web: pages/ElectionsPage.jsx → GET /elections/ (public). */
class ElectionsViewModel(private val publicRepository: PublicRepository) : ViewModel() {

    private val _state = MutableStateFlow(ElectionsUiState())
    val state: StateFlow<ElectionsUiState> = _state.asStateFlow()

    init { load(false) }

    fun refresh() = load(true)
    fun retry() = load(false)
    fun onQuery(q: String) = _state.update { it.copy(query = q) }
    fun onPhase(p: ElectionPhase?) = _state.update { it.copy(phase = p) }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRefreshing = refresh,
                    elections = if (refresh && it.elections is LoadState.Ready) it.elections else LoadState.Loading
                )
            }
            val result = publicRepository.fetchElections()
            val mapped = result.map { list -> withContext(Dispatchers.Default) { list.map { ElectionUi(it, phaseOf(it)) } } }
            _state.update { st ->
                st.copy(
                    isRefreshing = false,
                    elections = mapped.fold(
                        onSuccess = { LoadState.Ready(it.sortedBy { e -> e.phase.ordinal }) },
                        onFailure = { e ->
                            if (refresh && st.elections is LoadState.Ready) st.elections
                            else LoadState.Error(e.message ?: "Failed to load elections. Please try again later.")
                        }
                    )
                )
            }
        }
    }

    private fun phaseOf(e: ElectionItem): ElectionPhase {
        val status = e.status?.uppercase().orEmpty()
        val today = DisplayDates.today()
        val start = DisplayDates.parse(e.startDate)
        val end = DisplayDates.parse(e.endDate)
        return when {
            status.contains("CLOSED") || status.contains("COMPLETED") || status.contains("ENDED") || status == "INACTIVE" -> ElectionPhase.CLOSED
            end != null && end < today -> ElectionPhase.CLOSED
            start != null && start > today -> ElectionPhase.UPCOMING
            status.contains("UPCOMING") || status.contains("SCHEDULED") -> ElectionPhase.UPCOMING
            else -> ElectionPhase.ONGOING
        }
    }
}
