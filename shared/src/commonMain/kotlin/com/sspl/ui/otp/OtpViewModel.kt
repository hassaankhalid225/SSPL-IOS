package com.sspl.ui.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * WARNING (pre-existing): there is no phone-OTP endpoint in the backend / web contract. This VM
 * verifies against a hard-coded code locally and does not create a session. The login screen no
 * longer exposes the phone mode, so this screen is unreachable from the UI until a real endpoint
 * exists. Kept so the route and DI wiring stay intact.
 */
class OtpViewModel : ViewModel() {
    private val _resultFlow = MutableSharedFlow<OtpOneTimeEvents>()
    val resultFlow = _resultFlow.asSharedFlow()

    private val _state = MutableStateFlow(OtpState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        startResendTimer()
    }

    fun onEvent(event: OtpEvent) {
        when (event) {
            is OtpEvent.VerifyOtp -> verifyOtp(event.otp)
            is OtpEvent.ResendOtp -> resendOtp()
            is OtpEvent.OtpValueChanged -> _state.update { it.copy(otpValue = event.value) }
        }
    }

    private fun verifyOtp(otp: String) {
        if (_state.value.isVerifying) return
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true) }
            delay(VERIFICATION_DELAY)
            when (otp) {
                VALID_OTP -> _resultFlow.emit(OtpOneTimeEvents.Success)
                else -> _resultFlow.emit(OtpOneTimeEvents.ShowMessage("That code is not correct. Check the SMS and try again."))
            }
            _state.update { it.copy(isVerifying = false) }
        }
    }

    private fun resendOtp() {
        if (_state.value.resendSecondsLeft > 0) return
        viewModelScope.launch {
            _state.update { it.copy(otpValue = "") }
            _resultFlow.emit(OtpOneTimeEvents.ShowMessage("A new code has been sent."))
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (left in RESEND_SECONDS downTo 0) {
                _state.update { it.copy(resendSecondsLeft = left) }
                if (left > 0) delay(1000)
            }
        }
    }

    companion object {
        private const val VALID_OTP = "123456"
        private const val VERIFICATION_DELAY = 1500L
        private const val RESEND_SECONDS = 60
    }
}

data class OtpState(
    val isVerifying: Boolean = false,
    val otpValue: String = "",
    val resendSecondsLeft: Int = 0
)

sealed interface OtpEvent {
    data class VerifyOtp(val otp: String) : OtpEvent
    data object ResendOtp : OtpEvent
    data class OtpValueChanged(val value: String) : OtpEvent
}

sealed interface OtpOneTimeEvents {
    data class ShowMessage(val message: String) : OtpOneTimeEvents
    data object Success : OtpOneTimeEvents
}
