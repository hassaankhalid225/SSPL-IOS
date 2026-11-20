package com.sspl.ui.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.DirectoryMemberRecord
import com.sspl.core.models.DirectoryStatistics
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Web: components/comman/MembersDirectory.jsx FILTERS. */
enum class MemberFilter(val value: String, val label: String) {
    ALL("ALL", "All members"),
    LIFE("LIFE_MEMBER", "Life"),
    REGULAR("REGULAR_MEMBER", "Regular"),
    DECEASED("DECEASED_MEMBER", "Deceased")
}

data class MembersUiState(
    val members: LoadState<List<DirectoryMemberRecord>> = LoadState.Loading,
    val statistics: DirectoryStatistics? = null,
    val totalRecords: Int = 0,
    val filter: MemberFilter = MemberFilter.ALL,
    val query: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val loadingMore: Boolean = false,
    val loadMoreError: String? = null,
    val isRefreshing: Boolean = false
) {
    val canLoadMore: Boolean get() = members is LoadState.Ready && page < totalPages
}

/** GET /membership/statistics-and-list?page=&limit=10&role=&search= (public; search debounced 500 ms like web). */
class MembersDirectoryViewModel(private val repo: PublicRepository) : ViewModel() {

    private val _state = MutableStateFlow(MembersUiState())
    val state: StateFlow<MembersUiState> = _state.asStateFlow()
    private var job: Job? = null
    private var searchJob: Job? = null

    init { loadFirst(false) }

    fun refresh() = loadFirst(true)
    fun retry() = loadFirst(false)

    fun onFilter(f: MemberFilter) {
        if (f == _state.value.filter) return
        _state.update { it.copy(filter = f) }
        loadFirst(false)
    }

    fun onQuery(q: String) {
        _state.update { it.copy(query = q) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadFirst(false)
        }
    }

    private fun loadFirst(refresh: Boolean) {
        job?.cancel()
        job = viewModelScope.launch {
            val s = _state.value
            _state.update {
                it.copy(
                    isRefreshing = refresh,
                    loadingMore = false,
                    loadMoreError = null,
                    members = if (refresh && it.members is LoadState.Ready) it.members else LoadState.Loading
                )
            }
            val result = repo.fetchMembersDirectory(page = 1, role = s.filter.value, search = s.query, limit = PAGE_SIZE)
            _state.update { st ->
                result.fold(
                    onSuccess = { r ->
                        st.copy(
                            isRefreshing = false,
                            members = LoadState.Ready(r.records),
                            statistics = r.statistics,
                            totalRecords = r.meta.totalRecords,
                            page = 1,
                            totalPages = maxOf(1, r.meta.totalPages)
                        )
                    },
                    onFailure = { e ->
                        st.copy(
                            isRefreshing = false,
                            members = if (refresh && st.members is LoadState.Ready) st.members
                            else LoadState.Error(e.message ?: "Failed to load members from server.")
                        )
                    }
                )
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (!s.canLoadMore || s.loadingMore) return
        val current = (s.members as LoadState.Ready).data
        job = viewModelScope.launch {
            _state.update { it.copy(loadingMore = true, loadMoreError = null) }
            val next = s.page + 1
            val result = repo.fetchMembersDirectory(page = next, role = s.filter.value, search = s.query, limit = PAGE_SIZE)
            _state.update { st ->
                result.fold(
                    onSuccess = { r ->
                        st.copy(
                            loadingMore = false,
                            members = LoadState.Ready((current + r.records).distinctBy { it.id }),
                            page = next,
                            totalPages = maxOf(1, r.meta.totalPages),
                            totalRecords = r.meta.totalRecords
                        )
                    },
                    onFailure = { e -> st.copy(loadingMore = false, loadMoreError = e.message ?: "Failed to load more.") }
                )
            }
        }
    }

    private companion object { const val PAGE_SIZE = 15 }
}
