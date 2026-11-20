package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.PortalRepository
import com.sspl.core.repositories.PortalSubmission
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Web filter options: "" = all statuses. */
val SUBMISSION_FILTERS = listOf("" to "All", "PENDING" to "Pending", "ACCEPTED" to "Accepted", "REJECTED" to "Rejected")

data class SubmissionsList(
    val items: List<PortalSubmission>,
    val totalRecords: Int,
    val page: Int,
    val totalPages: Int
) {
    val canLoadMore get() = page < totalPages
}

data class MySubmissionsUiState(
    val filter: String = "",
    val list: LoadState<SubmissionsList> = LoadState.Loading,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadMoreError: String? = null
)

class MySubmissionsViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _state = MutableStateFlow(MySubmissionsUiState())
    val state: StateFlow<MySubmissionsUiState> = _state.asStateFlow()

    /** Per-filter cache: the endpoint is slow, so switching back to a loaded tab is instant. */
    private val cache = mutableMapOf<String, SubmissionsList>()
    private var job: Job? = null

    init { load(initial = true) }

    fun setFilter(status: String) {
        if (status == _state.value.filter) return
        val cached = cache[status]
        _state.update { it.copy(filter = status, list = cached?.let { c -> LoadState.Ready(c) } ?: LoadState.Loading, loadMoreError = null) }
        if (cached == null) load(initial = true)
    }

    fun retry() = load(initial = true)
    fun refresh() = load(initial = false)

    private fun load(initial: Boolean) {
        job?.cancel()
        val filter = _state.value.filter
        _state.update { if (initial) it.copy(list = LoadState.Loading) else it.copy(isRefreshing = true) }
        job = viewModelScope.launch {
            repository.getMySubmissions(status = filter.ifBlank { null }, page = 1)
                .onSuccess { p ->
                    val list = SubmissionsList(p.records, p.totalRecords, p.currentPage, p.totalPages)
                    cache[filter] = list
                    _state.update { it.copy(list = LoadState.Ready(list), isRefreshing = false) }
                }
                .onFailure { e ->
                    _state.update { s ->
                        if (!initial && s.list is LoadState.Ready) s.copy(isRefreshing = false)
                        else s.copy(list = LoadState.Error(e.message ?: "Failed to load submissions."), isRefreshing = false)
                    }
                }
        }
    }

    fun loadMore() {
        val s = _state.value
        val current = (s.list as? LoadState.Ready)?.data ?: return
        if (s.isLoadingMore || !current.canLoadMore) return
        val filter = s.filter
        _state.update { it.copy(isLoadingMore = true, loadMoreError = null) }
        viewModelScope.launch {
            repository.getMySubmissions(status = filter.ifBlank { null }, page = current.page + 1)
                .onSuccess { p ->
                    val merged = current.copy(
                        items = (current.items + p.records).distinctBy { it.item.id },
                        page = p.currentPage,
                        totalPages = p.totalPages,
                        totalRecords = p.totalRecords
                    )
                    cache[filter] = merged
                    _state.update { if (it.filter == filter) it.copy(list = LoadState.Ready(merged), isLoadingMore = false) else it.copy(isLoadingMore = false) }
                }
                .onFailure { e -> _state.update { it.copy(isLoadingMore = false, loadMoreError = e.message ?: "Couldn't load more") } }
        }
    }
}
