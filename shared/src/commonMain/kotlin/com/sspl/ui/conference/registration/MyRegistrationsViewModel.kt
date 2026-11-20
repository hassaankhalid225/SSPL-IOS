package com.sspl.ui.conference.registration

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.MyRegistrationsData
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
    val actionError: String? = null
)

sealed interface MyRegistrationsEvent {
    data class OpenPayment(val url: String, val registrationId: Long, val transactionId: String?) : MyRegistrationsEvent
}

/** D4: GET /external/events/my-registrations; retry payment via POST /external/events/registrations/{id}/pay. */
class MyRegistrationsViewModel(
    private val eventRepository: EventRepository
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

    fun retryPayment(reg: Registration) {
        if (_state.value.retryingId != null) return
        viewModelScope.launch {
            _state.update { it.copy(retryingId = reg.id, actionError = null) }
            val resp = eventRepository.retryEventPayment(reg.id).first()
            val url = resp.payment?.bestPaymentUrl
            _state.update { it.copy(retryingId = null) }
            if (resp.success && !url.isNullOrBlank()) {
                _events.send(MyRegistrationsEvent.OpenPayment(url, reg.id, resp.payment?.bestTransactionId))
            } else {
                _state.update {
                    it.copy(actionError = resp.message?.takeIf { m -> m.isNotBlank() } ?: "Couldn't start the payment. Please try again or contact the secretariat.")
                }
            }
        }
    }

    fun clearActionError() = _state.update { it.copy(actionError = null) }
}
