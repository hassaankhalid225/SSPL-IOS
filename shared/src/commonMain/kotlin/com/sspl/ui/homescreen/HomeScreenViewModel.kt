package com.sspl.ui.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.Banner
import com.sspl.core.models.EventItem
import com.sspl.core.models.FellowshipStats
import com.sspl.core.models.SocietyNewsItem
import com.sspl.core.push.PushNotificationService
import com.sspl.core.repositories.EventRepository
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.core.repositories.PublicRepository
import com.sspl.core.usecases.GetBannersUseCase
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val banners: LoadState<List<Banner>> = LoadState.Loading,
    /** Ongoing first, then upcoming by start date. */
    val events: LoadState<List<EventItem>> = LoadState.Loading,
    val news: List<SocietyNewsItem> = emptyList(),
    val stats: FellowshipStats? = null,
    val isRefreshing: Boolean = false
)

/**
 * Home data. All sections load in parallel and independently, so one slow endpoint never blocks
 * the others. Events share [EventRepository]'s cache with the Events tab (one multi-MB download).
 */
class HomeScreenViewModel(
    private val getBannersUseCase: GetBannersUseCase,
    private val pushNotificationService: PushNotificationService,
    private val publicRepository: PublicRepository,
    private val eventRepository: EventRepository,
    private val fellowshipRepository: FellowshipRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        // Instant paint from cache when returning to Home.
        eventRepository.cachedEvents(active = null)?.let { cached ->
            _state.update { it.copy(events = LoadState.Ready(pickHomeEvents(cached))) }
        }
        loadHomeData()
        viewModelScope.launch {
            runCatching { pushNotificationService.registerDeviceToken() }
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        loadHomeData(force = true)
    }

    fun loadHomeData(force: Boolean = false) {
        fetchBanners()
        fetchEvents(force)
        fetchNews()
        fetchStats()
    }

    fun retryEvents() {
        _state.update { it.copy(events = LoadState.Loading) }
        fetchEvents(force = true)
    }

    private fun fetchBanners() {
        viewModelScope.launch {
            getBannersUseCase().collect { s ->
                when (s) {
                    is ApiStates.Success -> {
                        // Behaviour frozen: SLIDER or untyped, active only. Carousel caps at 3.
                        val list = s.data?.banners.orEmpty()
                            .filter { it.isActive && (it.type == "SLIDER" || it.type == null) }
                        _state.update { it.copy(banners = LoadState.Ready(list)) }
                    }
                    is ApiStates.Failure -> _state.update { it.copy(banners = LoadState.Ready(emptyList())) }
                    else -> Unit
                }
            }
        }
    }

    private fun fetchEvents(force: Boolean) {
        viewModelScope.launch {
            val result = eventRepository.loadEvents(active = null, forceRefresh = force)
            _state.update { st ->
                st.copy(
                    isRefreshing = false,
                    events = result.fold(
                        onSuccess = { LoadState.Ready(pickHomeEvents(it)) },
                        onFailure = {
                            // Keep showing what we had if a refresh fails.
                            if (st.events is LoadState.Ready) st.events
                            else LoadState.Error("Check your connection and try again.")
                        }
                    )
                )
            }
        }
    }

    private fun fetchNews() {
        viewModelScope.launch {
            publicRepository.getSocietyNews().collect { list ->
                _state.update { it.copy(news = list.filter { n -> n.isActive && n.isVisible }.take(4)) }
            }
        }
    }

    private fun fetchStats() {
        viewModelScope.launch {
            fellowshipRepository.getPublicStats().collect { s ->
                _state.update { it.copy(stats = s) }
            }
        }
    }

    private fun pickHomeEvents(all: List<EventItem>): List<EventItem> {
        val ongoing = all.filter { it.computedStatus == "ongoing" }
        val upcoming = all.filter { it.computedStatus == "upcoming" }.sortedBy { it.effectiveStart ?: "9999" }
        return (ongoing + upcoming).take(8)
    }
}
