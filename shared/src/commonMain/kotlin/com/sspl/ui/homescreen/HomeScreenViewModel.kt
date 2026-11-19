package com.sspl.ui.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.Banner
import com.sspl.core.models.PublicEventItem
import com.sspl.core.models.SocietyNewsItem
import com.sspl.core.push.PushNotificationService
import com.sspl.core.repositories.PublicRepository
import com.sspl.core.usecases.GetBannersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val getBannersUseCase: GetBannersUseCase,
    private val pushNotificationService: PushNotificationService,
    private val publicRepository: PublicRepository
) : ViewModel() {
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners = _banners.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<PublicEventItem>>(emptyList())
    val upcomingEvents = _upcomingEvents.asStateFlow()

    private val _latestNews = MutableStateFlow<List<SocietyNewsItem>>(emptyList())
    val latestNews = _latestNews.asStateFlow()

    init {
        loadHomeData()
        viewModelScope.launch {
            try {
                pushNotificationService.registerDeviceToken()
            } catch (e: Exception) {
                println("Push token registration skipped/error: ${e.message}")
            }
        }
    }

    fun loadHomeData() {
        fetchBanners()
        fetchUpcomingEvents()
        fetchSocietyNews()
    }

    fun fetchBanners() {
        viewModelScope.launch {
            getBannersUseCase().collect { state ->
                when (state) {
                    is ApiStates.Success -> {
                        val activeBanners = state.data?.banners?.filter { it.isActive } ?: emptyList()
                        _banners.value = activeBanners
                    }
                    else -> {}
                }
            }
        }
    }

    fun fetchUpcomingEvents() {
        viewModelScope.launch {
            publicRepository.getUpcomingEvents().collect { events ->
                _upcomingEvents.value = events.take(6)
            }
        }
    }

    fun fetchSocietyNews() {
        viewModelScope.launch {
            publicRepository.getSocietyNews().collect { news ->
                _latestNews.value = news.take(4)
            }
        }
    }
}
