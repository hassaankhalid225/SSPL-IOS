package com.sspl.ui.conference.registration

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.InitiatePaymentRequest
import com.sspl.core.models.MyRegistrationsData
import com.sspl.core.usecases.InitiatePaymentUseCase
import com.sspl.session.UserSession
import com.sspl.core.models.Registration
import com.sspl.core.repositories.EventRepository
import com.sspl.ui.conference.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegistrationFilter(val label: String) { ALL("All"), CONFERENCES("Conferences"), WORKSHOPS("Workshops"), MEETINGS("Meetings") }

@Immutable
data class MyRegistrationsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val data: MyRegistrationsData? = null,
    val filter: RegistrationFilter = RegistrationFilter.ALL,
    val all: List<Registration> = emptyList(),
    val visible: List<Registration> = emptyList(),
    val counts: List<Int> = listOf(0, 0, 0, 0),
    val retryingId: Long? = null,
    val actionError: String? = null,
    /** Web `alert(...)` (invalid / missing account email). */
    val alertMessage: String? = null,
    /** Web PhoneModal: registration waiting for a mobile number. */
    val phonePromptFor: Registration? = null,
    val phonePromptInitial: String = ""
)

sealed interface MyRegistrationsEvent {
    data class OpenPayment(val url: String, val registrationId: Long, val transactionId: String?) : MyRegistrationsEvent
}

/** D4: GET /external/events/my-registrations; retry payment via POST /swich/initiate-payment (web parity). */
class MyRegistrationsViewModel(
    private val eventRepository: EventRepository,
    private val initiatePaymentUseCase: InitiatePaymentUseCase,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(MyRegistrationsState())
    val state: StateFlow<MyRegistrationsState> = _state.asStateFlow()

    private val _events = Channel<MyRegistrationsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun refresh() = load(refresh = true)

    fun load(refresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.data == null, isRefreshing = refresh && it.data != null, error = null) }
            eventRepository.loadMyRegistrations()
                .onSuccess { d ->
                    val all = d.all.ifEmpty {
                        (d.conferences + d.workshops + d.monthlyMeetings + d.conferenceEvents).distinctBy { it.id }
                    }.sortedByDescending { it.createdAt ?: "" }
                    _state.update {
                        recompute(it.copy(data = d, all = all, isLoading = false, isRefreshing = false))
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = e.toUserMessage("Your registrations couldn't be loaded. Try again.")
                        )
                    }
                }
        }
    }

    fun setFilter(f: RegistrationFilter) = _state.update { recompute(it.copy(filter = f)) }

    private fun recompute(s: MyRegistrationsState): MyRegistrationsState {
        val d = s.data ?: return s
        val lists = listOf(s.all, d.conferences, d.workshops, d.monthlyMeetings)
        return s.copy(visible = lists[s.filter.ordinal], counts = lists.map { it.size })
    }

    /**
     * Web MyRegistrationsView.handleRetryPayment: account email must be valid (else alert), phone
     * must be valid (else ask for it), msisdn via toPkMsisdn, then POST /swich/initiate-payment.
     */
    fun retryPayment(reg: Registration) {
        if (_state.value.retryingId != null) return
        viewModelScope.launch {
            val user = userSession.getUser()
            val email = (user?.account?.email ?: "").trim()
            if (!PaymentContact.isValidEmail(email)) {
                _state.update {
                    it.copy(
                        alertMessage = "Your profile has an invalid or missing email address (${email.ifBlank { "None detected" }}). " +
                            "Please update your profile or contact support."
                    )
                }
                return@launch
            }
            val phone = (user?.account?.phone ?: "").trim()
            if (phone.isNotEmpty() && PaymentContact.isValidPhone(phone)) {
                proceedWithPayment(reg, PaymentContact.toPkMsisdn(phone), email)
            } else {
                _state.update { it.copy(phonePromptFor = reg, phonePromptInitial = phone) }
            }
        }
    }

    /** Phone dialog submit (web PhoneModal): validate, convert, continue. Returns an inline error or null. */
    fun submitPhone(phone: String): String? {
        if (!PaymentContact.isValidPhone(phone)) return "Please enter a valid mobile number"
        val reg = _state.value.phonePromptFor ?: return null
        _state.update { it.copy(phonePromptFor = null) }
        viewModelScope.launch {
            val email = (userSession.getUser()?.account?.email ?: "").trim()
            proceedWithPayment(reg, PaymentContact.toPkMsisdn(phone), email)
        }
        return null
    }

    fun dismissPhonePrompt() = _state.update { it.copy(phonePromptFor = null) }
    fun dismissAlert() = _state.update { it.copy(alertMessage = null) }

    private suspend fun proceedWithPayment(reg: Registration, msisdn: String, email: String) {
        val user = userSession.getUser()
        _state.update { it.copy(retryingId = reg.id, actionError = null) }
        val request = InitiatePaymentRequest(
            amount = reg.feeAmount,
            item = "${reg.conference?.title?.takeIf { it.isNotBlank() } ?: "Event"} - ${reg.role?.name?.takeIf { it.isNotBlank() } ?: "Participant"}",
            msisdn = msisdn,
            name = "${user?.firstName} ${user?.lastName}",
            email = email,
            userId = user?.id,
            conferenceRegistrationId = reg.id
        )
        val result = initiatePaymentUseCase(request).first { it !is ApiStates.Loading && it !is ApiStates.Idle }
        _state.update { it.copy(retryingId = null) }
        when (result) {
            is ApiStates.Success -> {
                val url = result.data?.bestPaymentUrl
                if (!url.isNullOrBlank()) {
                    _events.send(MyRegistrationsEvent.OpenPayment(url, reg.id, result.data?.bestTransactionId))
                } else {
                    _state.update { it.copy(actionError = "Failed to initiate payment: No payment URL received") }
                }
            }
            is ApiStates.Failure -> _state.update {
                it.copy(actionError = "Failed to initiate payment: ${result.error.message?.takeIf { m -> m.isNotBlank() } ?: "Unknown error"}")
            }
            else -> Unit
        }
    }

    fun clearActionError() = _state.update { it.copy(actionError = null) }
}
