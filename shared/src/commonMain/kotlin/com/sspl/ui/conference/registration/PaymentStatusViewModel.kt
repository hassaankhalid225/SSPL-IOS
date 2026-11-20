package com.sspl.ui.conference.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class PaymentVerificationUiState(
    val status: String = "processing", // "processing" | "awaiting" | "success" | "failed" | "pending"
    val message: String = "Verifying payment details...",
    val isLoading: Boolean = false,
    val retryPaymentUrl: String? = null,
    val transactionId: String? = null,
    val reopenUrl: String? = null
)

class PaymentStatusViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentVerificationUiState())
    val uiState: StateFlow<PaymentVerificationUiState> = _uiState.asStateFlow()

    fun verifyTransaction(customerTransactionId: String) {
        if (customerTransactionId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                transactionId = customerTransactionId,
                status = "processing",
                message = "Verifying payment with payment gateway...",
                isLoading = true
            )
            eventRepository.verifySwichPayment(customerTransactionId).collectLatest { response ->
                val isSuccess = response.success &&
                        (response.status?.lowercase() == "success" || response.status == "0000" || response.status == "00")
                val isPending = response.status?.lowercase() == "pending"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = if (isSuccess) "success" else if (isPending) "pending" else "failed",
                    message = if (isSuccess) {
                        "Your transaction has been verified successfully! Registration is confirmed."
                    } else if (isPending) {
                        "Your payment is currently being processed or pending verification. Please check back shortly."
                    } else {
                        response.message ?: "Transaction verification failed or payment was declined."
                    }
                )
            }
        }
    }

    /** Payment page was opened in the browser; wait for the user to come back and check. */
    fun setAwaiting(transactionId: String?) {
        if (_uiState.value.status != "processing") return
        _uiState.value = _uiState.value.copy(
            status = "awaiting",
            transactionId = transactionId,
            isLoading = false,
            message = "If you finished paying, check the status here. Confirmation can take a minute."
        )
    }

    fun checkAwaiting() {
        val id = _uiState.value.transactionId ?: return
        verifyTransaction(id)
    }

    fun setExplicitStatus(status: String, message: String) {
        _uiState.value = _uiState.value.copy(
            status = status,
            message = message,
            isLoading = false
        )
    }

    fun retryPayment(registrationId: Long, onPaymentUrlReceived: (String) -> Unit) {
        if (registrationId <= 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            eventRepository.retryEventPayment(registrationId).collectLatest { response ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                val pUrl = response.payment?.bestPaymentUrl
                if (response.success && !pUrl.isNullOrBlank()) {
                    val txn = response.payment?.bestTransactionId
                    _uiState.value = _uiState.value.copy(
                        status = "awaiting",
                        transactionId = txn ?: _uiState.value.transactionId,
                        message = "If you finished paying, check the status here. Confirmation can take a minute."
                    )
                    onPaymentUrlReceived(pUrl)
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = "failed",
                        message = response.message ?: "Unable to re-initiate payment. Please contact secretariat."
                    )
                }
            }
        }
    }
}

