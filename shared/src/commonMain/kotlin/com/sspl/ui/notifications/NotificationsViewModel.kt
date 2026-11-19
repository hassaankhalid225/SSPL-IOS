package com.sspl.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.NotificationItem
import com.sspl.core.models.SocietyNewsItem
import com.sspl.core.repositories.NotificationRepository
import com.sspl.core.repositories.PublicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val publicRepository: PublicRepository
) : ViewModel() {

    private val _societyNews = MutableStateFlow<List<SocietyNewsItem>>(emptyList())
    val societyNews: StateFlow<List<SocietyNewsItem>> = _societyNews.asStateFlow()

    private val _isLoadingNews = MutableStateFlow(false)
    val isLoadingNews: StateFlow<Boolean> = _isLoadingNews.asStateFlow()

    init {
        loadSocietyNews()
    }

    fun loadSocietyNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            publicRepository.getSocietyNews().collect { list ->
                _societyNews.value = list
                _isLoadingNews.value = false
            }
        }
    }

    val notifications: StateFlow<List<NotificationItem>> = notificationRepository.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.clearAll()
        }
    }
}
