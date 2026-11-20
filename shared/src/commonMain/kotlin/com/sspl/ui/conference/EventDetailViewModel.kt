package com.sspl.ui.conference

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.EventCheckData
import com.sspl.core.models.EventItem
import com.sspl.core.models.Session
import com.sspl.core.repositories.EventRepository
import com.sspl.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class EventDetailUiState(
    val eventId: Long = 0L,
    val isLoading: Boolean = true,
    val event: EventItem? = null,
    val card: EventCardUi? = null,
    val sessions: List<Session> = emptyList(),
    val loadError: String? = null,
    val isGuest: Boolean = true,
    val registrationData: EventCheckData? = null,
    val isCheckingRegistration: Boolean = false,
    val relatedWorkshops: List<EventCardUi> = emptyList(),
    // registration action
    val isRegistering: Boolean = false,
    val registrationSuccess: Boolean = false,
    val registrationPending: Boolean = false,
    val registrationMessage: String? = null,
    val paymentUrl: String? = null,
    val transactionId: String? = null,
    val registrationId: Long? = null,
    val errorMessage: String? = null
) {
    val minFee: Double?
        get() = event?.roles?.filter { it.isActive }?.minOfOrNull { it.currentFee }
}

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val userSession: UserSession
) : ViewModel() {

    private var loadedId: Long = -1L
    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean
        get() = userSession.userFlow.value?.let { !it.isGuest() } == true && !userSession.token.isNullOrBlank()

    /**
     * Resolve the event from the shared in-memory list first (instant), then refresh from
     * GET /external/events/{id}. Safe to call repeatedly with the same id.
     */
    fun loadEvent(eventId: Long, force: Boolean = false) {
        if (eventId <= 0L) {
            _uiState.update { it.copy(isLoading = false, loadError = "This event link is invalid.") }
            return
        }
        if (!force && loadedId == eventId) return
        loadedId = eventId
        _uiState.update { it.copy(eventId = eventId, isGuest = !isLoggedIn, loadError = null, isLoading = it.event == null) }

        viewModelScope.launch {
            eventRepository.cachedEvents(active = null)?.firstOrNull { it.effectiveId == eventId }?.let { cached ->
                applyEvent(cached)
            }
            val fresh = runCatching { eventRepository.getEventById(eventId).first() }.getOrNull()
            if (fresh != null) {
                // Detail payload may omit the image; keep the one from the list if so.
                val merged = if (fresh.effectiveImage == null) {
                    _uiState.value.event?.imageUrl?.let { fresh.copy(imageUrl = it) } ?: fresh
                } else fresh
                applyEvent(merged)
            } else if (_uiState.value.event == null) {
                _uiState.update {
                    it.copy(isLoading = false, loadError = "We couldn't load this event. Check your connection and try again.")
                }
                return@launch
            }
            if (isLoggedIn) checkRegistration(eventId)
            loadRelatedWorkshops()
        }
    }

    fun retry() = loadEvent(_uiState.value.eventId, force = true)

    private suspend fun applyEvent(e: EventItem) {
        val (card, sessions) = withContext(Dispatchers.Default) {
            e.toCardUi() to e.sessions.sortedBy { it.startsAt ?: "" }
        }
        _uiState.update { it.copy(event = e, card = card, sessions = sessions, isLoading = false, loadError = null) }
    }

    fun checkRegistration(eventId: Long = _uiState.value.eventId) {
        if (!isLoggedIn || eventId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingRegistration = true) }
            val data = runCatching { eventRepository.checkEventStatus(eventId).first() }.getOrNull()
            _uiState.update { it.copy(registrationData = data, isCheckingRegistration = false) }
        }
    }

    /** Related workshops for conferences — derived from the cached all-events list (no extra download). */
    private fun loadRelatedWorkshops() {
        val type = (_uiState.value.event?.type ?: "").uppercase()
        if (!(type.contains("CONFERENCE") || type.contains("PROGRAM"))) return
        viewModelScope.launch {
            val all = eventRepository.cachedEvents(active = null)
                ?: eventRepository.loadEvents(active = null).getOrNull()
                ?: return@launch
            val workshops = withContext(Dispatchers.Default) {
                all.asSequence()
                    .filter { (it.type ?: "").uppercase() == "WORKSHOP" }
                    .map { it.toCardUi() }
                    .filter { it.status != "past" }
                    .sortedBy { it.startKey }
                    .take(6)
                    .toList()
            }
            _uiState.update { it.copy(relatedWorkshops = workshops) }
        }
    }

    /** POST /external/events/{id}/register — same payload as web (`role_id`). */
    fun register(roleId: Long?) {
        val currentEvent = _uiState.value.event ?: return
        if (_uiState.value.isRegistering) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true, errorMessage = null) }
            val response = eventRepository.registerForEvent(currentEvent.id, roleId).first()
            if (response.success) {
                val pUrl = response.payment?.bestPaymentUrl
                val isPending = response.payment?.pendingApproval == true ||
                    response.registration?.paymentStatus == "PENDING"
                _uiState.update {
                    it.copy(
                        isRegistering = false,
                        registrationSuccess = true,
                        registrationPending = isPending,
                        registrationMessage = if (isPending) {
                            "Your registration has been submitted and is pending admin approval. You'll get a confirmation email once approved."
                        } else {
                            "Registration successful! Your registration is confirmed."
                        },
                        paymentUrl = pUrl,
                        transactionId = response.payment?.bestTransactionId,
                        registrationId = response.registration?.id
                    )
                }
                if (pUrl.isNullOrBlank()) checkRegistration(currentEvent.id)
            } else {
                _uiState.update {
                    it.copy(
                        isRegistering = false,
                        errorMessage = response.message?.takeIf { m -> m.isNotBlank() && !m.contains("Exception") }
                            ?: "Registration failed. Please try again."
                    )
                }
            }
        }
    }

    /** Called once the payment URL has been handed to the browser so it isn't re-opened. */
    fun onPaymentUrlConsumed() {
        _uiState.update { it.copy(paymentUrl = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, registrationMessage = null) }
    }
}
