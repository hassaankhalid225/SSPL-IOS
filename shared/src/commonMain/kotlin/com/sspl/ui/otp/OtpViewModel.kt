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
 * Phone-OTP sign-in is NOT part of the backend contract: the web's OtpPage.jsx calls
 * `/verify/send-otp` and `/verify/verify-otp` on a different service (VITE_API_URL_BASE, cookie
 * auth for "subscribers"), not the SSPL portal API. This screen is unreachable from the UI (the
 * login screen doesn't expose phone mode). No code is ever accepted locally and no fake "code
 * sent" message is shown — every attempt explains that phone sign-in is unavailable.
 */
class OtpViewModel : ViewModel() {
    private val _resultFlow = MutableSharedFlow<OtpOneTimeEvents>()
    val resultFlow = _resultFlow.asSharedFlow()

    private val _state = MutableStateFlow(OtpState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null


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
            _resultFlow.emit(OtpOneTimeEvents.ShowMessage(UNAVAILABLE))
        }
    }

    private fun resendOtp() {
        if (_state.value.resendSecondsLeft > 0) return
        viewModelScope.launch {
            _state.update { it.copy(otpValue = "") }
            _resultFlow.emit(OtpOneTimeEvents.ShowMessage(UNAVAILABLE))
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
        private const val UNAVAILABLE =
            "Phone sign-in isn't available. Please sign in with your email and password."
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
