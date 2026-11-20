package com.sspl.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.NotificationItem
import com.sspl.core.models.SocietyNewsItem
import com.sspl.core.repositories.NotificationRepository
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val news: LoadState<List<SocietyNewsItem>> = LoadState.Loading,
    val isRefreshing: Boolean = false
)

/**
 * Announcements (web: /news → GET /society-news) + on-device app alerts (push / live sessions).
 */
class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val publicRepository: PublicRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    init {
        load(refresh = false)
    }

    fun refresh() = load(refresh = true)
    fun retry() = load(refresh = false)

    /** Kept for existing callers. */
    fun loadSocietyNews() = refresh()

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRefreshing = refresh,
                    news = if (refresh && it.news is LoadState.Ready) it.news else LoadState.Loading
                )
            }
            val result = publicRepository.fetchSocietyNews()
            _state.update { st ->
                st.copy(
                    isRefreshing = false,
                    news = result.fold(
                        onSuccess = { LoadState.Ready(it) },
                        onFailure = { e ->
                            if (refresh && st.news is LoadState.Ready) st.news
                            else LoadState.Error(e.message ?: "Failed to load announcements.")
                        }
                    )
                )
            }
        }
    }

    val notifications: StateFlow<List<NotificationItem>> = notificationRepository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markAsRead(notificationId: String) {
        viewModelScope.launch { notificationRepository.markAsRead(notificationId) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { notificationRepository.markAllAsRead() }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch { notificationRepository.deleteNotification(notificationId) }
    }

    fun clearAll() {
        viewModelScope.launch { notificationRepository.clearAll() }
    }
}
