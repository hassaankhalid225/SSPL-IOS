package com.sspl.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.DownloadItem
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DownloadCategory(val value: String, val label: String, val description: String = "")

/** Web: pages/DownloadsPage.jsx CATEGORIES. */
val DOWNLOAD_CATEGORIES = listOf(
    DownloadCategory("", "All"),
    DownloadCategory("CASE_PRESENTATION", "Case presentation", "Formats for clinical case presentations and discussions."),
    DownloadCategory("MDT_PRESENTATION", "MDT presentation", "Structured templates for multidisciplinary team case presentations and discussions."),
    DownloadCategory("RESEARCH_PRESENTATION", "Research presentation", "Slide templates for research studies and scientific presentations."),
    DownloadCategory("ABSTRACT", "Abstracts", "Formats for event abstracts, oral presentations, and poster submissions."),
    DownloadCategory("E_POSTER", "E-posters", "Pictorial A4 portrait templates for research and case-report posters, with presenter information."),
    DownloadCategory("SPEAKER_PROFILE", "Speaker profile", "A template for a short biography, photograph, and presentation title."),
    DownloadCategory("LOGBOOK", "Logbooks", "Training, procedure, and surgical case logbooks."),
    DownloadCategory("GUIDELINES_BROCHURES", "Guidelines & brochures", "Clinical and training guidelines, program brochures, and event information."),
    DownloadCategory("DOCUMENTS", "Documents", "Official society documents, policies, forms, and publications."),
    DownloadCategory("OTHERS", "Others", "Additional resources, supporting materials, and miscellaneous templates.")
)

fun categoryLabel(value: String?): String? = DOWNLOAD_CATEGORIES.firstOrNull { it.value == value && it.value.isNotEmpty() }?.label

data class DownloadsUiState(
    val items: LoadState<List<DownloadItem>> = LoadState.Loading,
    val category: String = "",
    val query: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val loadingMore: Boolean = false,
    val loadMoreError: String? = null,
    val isRefreshing: Boolean = false
) {
    val canLoadMore: Boolean get() = items is LoadState.Ready && page < totalPages
}

/** GET /downloads?category=&search=&page=&limit=12 (public). */
class DownloadsViewModel(private val repo: PublicRepository) : ViewModel() {

    private val _state = MutableStateFlow(DownloadsUiState())
    val state: StateFlow<DownloadsUiState> = _state.asStateFlow()
    private var job: Job? = null
    private var searchJob: Job? = null

    init { loadFirst(false) }

    fun refresh() = loadFirst(true)
    fun retry() = loadFirst(false)

    fun onCategory(value: String) {
        if (value == _state.value.category) return
        _state.update { it.copy(category = value) }
        loadFirst(false)
    }

    fun onQuery(q: String) {
        _state.update { it.copy(query = q) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(450)
            loadFirst(false)
        }
    }

    fun fileUrl(item: DownloadItem, download: Boolean) = repo.downloadFileUrl(item.id, download)

    private fun loadFirst(refresh: Boolean) {
        job?.cancel()
        job = viewModelScope.launch {
            val s = _state.value
            _state.update {
                it.copy(
                    isRefreshing = refresh,
                    loadMoreError = null,
                    loadingMore = false,
                    items = if (refresh && it.items is LoadState.Ready) it.items else LoadState.Loading
                )
            }
            val result = repo.fetchDownloads(category = s.category, search = s.query.trim(), page = 1, limit = PAGE_SIZE)
            _state.update { st ->
                result.fold(
                    onSuccess = { r ->
                        st.copy(
                            isRefreshing = false,
                            items = LoadState.Ready(r.items),
                            page = 1,
                            totalPages = maxOf(1, r.meta.totalPages),
                            total = r.meta.total
                        )
                    },
                    onFailure = { e ->
                        st.copy(
                            isRefreshing = false,
                            items = if (refresh && st.items is LoadState.Ready) st.items
                            else LoadState.Error(e.message ?: "Failed to load downloads.")
                        )
                    }
                )
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (!s.canLoadMore || s.loadingMore) return
        val current = (s.items as LoadState.Ready).data
        job = viewModelScope.launch {
            _state.update { it.copy(loadingMore = true, loadMoreError = null) }
            val next = s.page + 1
            val result = repo.fetchDownloads(category = s.category, search = s.query.trim(), page = next, limit = PAGE_SIZE)
            _state.update { st ->
                result.fold(
                    onSuccess = { r ->
                        st.copy(
                            loadingMore = false,
                            items = LoadState.Ready((current + r.items).distinctBy { it.id }),
                            page = next,
                            totalPages = maxOf(1, r.meta.totalPages),
                            total = r.meta.total
                        )
                    },
                    onFailure = { e -> st.copy(loadingMore = false, loadMoreError = e.message ?: "Failed to load more.") }
                )
            }
        }
    }

    private companion object { const val PAGE_SIZE = 12 }
}
