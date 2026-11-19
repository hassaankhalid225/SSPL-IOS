package com.sspl.ui.conference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.EventCheckData
import com.sspl.core.models.EventItem
import com.sspl.core.models.EventRegisterResponse
import com.sspl.core.repositories.EventRepository
import com.sspl.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: EventItem? = null,
    val registrationData: EventCheckData? = null,
    val isRegistering: Boolean = false,
    val registrationSuccess: Boolean = false,
    val registrationMessage: String? = null,
    val paymentUrl: String? = null,
    val transactionId: String? = null,
    val relatedWorkshops: List<EventItem> = emptyList(),
    val errorMessage: String? = null
)

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = !userSession.token.isNullOrBlank()

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            eventRepository.getEventById(eventId).collectLatest { eventItem ->
                if (eventItem == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Event not found or failed to load."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        event = eventItem
                    )

                    // If logged in, check registration status
                    if (isLoggedIn) {
                        checkRegistration(eventId)
                    }

                    // If event is Conference or Program, load related workshops
                    val type = (eventItem.type ?: "").uppercase()
                    if (type.contains("CONFERENCE") || type.contains("PROGRAM")) {
                        loadRelatedWorkshops()
                    }
                }
            }
        }
    }

    private fun checkRegistration(eventId: Long) {
        viewModelScope.launch {
            eventRepository.checkEventStatus(eventId).collectLatest { checkData ->
                _uiState.value = _uiState.value.copy(registrationData = checkData)
            }
        }
    }

    private fun loadRelatedWorkshops() {
        viewModelScope.launch {
            eventRepository.getEvents(type = "WORKSHOP", active = true).collectLatest { workshops ->
                _uiState.value = _uiState.value.copy(relatedWorkshops = workshops)
            }
        }
    }

    fun register(roleId: Long?, onPaymentRedirect: ((String) -> Unit)? = null) {
        val currentEvent = _uiState.value.event ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true, errorMessage = null)
            eventRepository.registerForEvent(currentEvent.id, roleId).collectLatest { response ->
                _uiState.value = _uiState.value.copy(isRegistering = false)
                if (response.success) {
                    val pUrl = response.payment?.bestPaymentUrl
                    val txnId = response.payment?.bestTransactionId
                    val isPending = response.payment?.pendingApproval == true ||
                            response.registration?.paymentStatus == "PENDING"

                    _uiState.value = _uiState.value.copy(
                        registrationSuccess = true,
                        registrationMessage = if (isPending) {
                            "Your registration has been submitted and is pending approval."
                        } else {
                            "Registration successful! Your registration is confirmed."
                        },
                        paymentUrl = pUrl,
                        transactionId = txnId
                    )

                    if (!pUrl.isNullOrBlank()) {
                        onPaymentRedirect?.invoke(pUrl)
                    } else {
                        // Refresh registration status
                        checkRegistration(currentEvent.id)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message ?: "Registration failed. Please try again."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            registrationMessage = null
        )
    }
}

