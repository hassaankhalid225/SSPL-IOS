package com.sspl.ui.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.InitResetPasswordUseCase
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.UpdatePasswordUseCase
import com.sspl.core.push.PushNotificationService
import com.sspl.session.UserSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Forgot / reset password. Contract = web `services/authApi.js` + `pages/auth/ForgotPasswordPage.jsx`:
 *  1. POST /auth/reset-password/init { email: trimmed + lower-cased } -> { token }
 *  2. POST /auth/reset-password { code (6 digits), password (>= 8), token }
 */
class ForgotPasswordViewModel(
    private val emailValidationUseCase: EmailValidationUseCase,
    private val initResetPasswordUseCase: InitResetPasswordUseCase,
    private val passwordValidationUseCase: PasswordValidationUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val userSession: UserSession,
    private val pushNotificationService: PushNotificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _initPasswordStatus = MutableSharedFlow<ApiStates<Any>>()
    val initPasswordStatus: SharedFlow<ApiStates<Any>> = _initPasswordStatus.asSharedFlow()

    private val _updatePasswordStatus = MutableSharedFlow<ApiStates<Any>>()
    val updatePasswordStatus: SharedFlow<ApiStates<Any>> = _updatePasswordStatus.asSharedFlow()

    private var timerJob: Job? = null

    fun onEvent(event: ForgotPasswordUiEvents) {
        when (event) {
            ForgotPasswordUiEvents.InitPassword -> sendCode()

            is ForgotPasswordUiEvents.EmailChanged -> {
                _uiState.update { it.copy(emailText = event.email, emailError = null, formError = null) }
                validateEmail()
            }

            is ForgotPasswordUiEvents.NewPasswordChanged -> {
                val isValid = passwordValidationUseCase(event.password) // >= 8, same as web reset page
                _uiState.update {
                    it.copy(
                        newPassword = event.password,
                        newPasswordError = if (!isValid) "Password must be at least 8 characters." else null,
                        confirmPasswordError = if (it.confirmPassword.isNotEmpty() && it.confirmPassword != event.password)
                            "Passwords do not match." else null,
                        formError = null
                    )
                }
                updateOtpPasswordButtonState()
            }

            is ForgotPasswordUiEvents.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.password,
                        confirmPasswordError = if (event.password != it.newPassword) "Passwords do not match." else null,
                        formError = null
                    )
                }
                updateOtpPasswordButtonState()
            }

            ForgotPasswordUiEvents.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            ForgotPasswordUiEvents.ToggleReenteredPasswordVisibility ->
                _uiState.update { it.copy(isReenteredPasswordVisible = !it.isReenteredPasswordVisible) }

            is ForgotPasswordUiEvents.OnOtpEntered -> {
                val digits = event.otp.filter { it.isDigit() }.take(OTP_LENGTH)
                _uiState.update { it.copy(otpString = digits, isOtpValid = digits.length == OTP_LENGTH, formError = null) }
                updateOtpPasswordButtonState()
            }

            ForgotPasswordUiEvents.UpdatePassword -> resetPassword()
        }
    }

    private fun sendCode() {
        val s = _uiState.value
        if (s.isLoadingInitPassword) return
        val email = s.emailText.trim().lowercase()
        if (!emailValidationUseCase(email)) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            return
        }
        viewModelScope.launch {
            initResetPasswordUseCase(email).collect { state ->
                _uiState.update { it.copy(isLoadingInitPassword = state is ApiStates.Loading) }

                var isHandled = false
                if (state is ApiStates.Failure) {
                    val msg = "${state.error.message} ${state.error.error.orEmpty()}".lowercase()
                    // Same mapping as web parseApiError()
                    if (msg.contains("not found") || msg.contains("does not exist") ||
                        msg.contains("no user") || msg.contains("no account") || msg.contains("unauthorized")
                    ) {
                        _uiState.update {
                            it.copy(
                                emailError = "Account does not exist with this email.",
                                formError = "Account does not exist with this email. Please create an account."
                            )
                        }
                        isHandled = true
                    }
                }
                if (!isHandled) _initPasswordStatus.emit(state)

                if (state is ApiStates.Success) {
                    _uiState.update {
                        it.copy(
                            showOtpView = true,
                            passwordToken = state.data?.token,
                            otpString = "",
                            infoMessage = "A 6-digit code has been sent to $email. It expires in about 5 minutes."
                        )
                    }
                    startResendTimer()
                }
            }
        }
    }

    private fun resetPassword() {
        val s = _uiState.value
        if (s.isLoadingUpdatePassword) return
        // Same checks/messages as web handleResetPassword()
        val error = when {
            s.otpString.length != OTP_LENGTH -> "Enter the full 6-digit code from your email."
            !passwordValidationUseCase(s.newPassword) -> "Password must be at least 8 characters."
            s.newPassword != s.confirmPassword -> "Passwords do not match."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(formError = error) }
            return
        }
        viewModelScope.launch {
            updatePasswordUseCase(s.otpString, s.newPassword, s.passwordToken.orEmpty()).collect { state ->
                _uiState.update { it.copy(isLoadingUpdatePassword = state is ApiStates.Loading) }
                if (state is ApiStates.Failure) {
                    _uiState.update {
                        it.copy(formError = state.error.message.ifBlank { "Could not reset password. Check the code and try again." })
                    }
                } else if (state is ApiStates.Success) {
                    // Web ForgotPasswordPage.jsx: when the reset returns an access token the user is
                    // signed in directly (tokens + user stored) instead of being sent to sign-in.
                    val auth = state.data
                    val token = auth?.accessToken
                    if (!token.isNullOrBlank()) {
                        userSession.setToken(token)
                        auth.refreshToken?.let { userSession.setRefreshToken(it) }
                        auth.user?.let { userSession.setUser(it) }
                        runCatching { pushNotificationService.registerDeviceToken() }
                        _uiState.update { it.copy(signedInDirectly = true) }
                    }
                    _updatePasswordStatus.emit(state)
                } else {
                    _updatePasswordStatus.emit(state)
                }
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (left in RESEND_SECONDS downTo 0) {
                _uiState.update { it.copy(resendSecondsLeft = left) }
                if (left > 0) delay(1000)
            }
        }
    }

    fun switchView(showOtpView: Boolean) {
        _uiState.update { it.copy(showOtpView = showOtpView, formError = null, infoMessage = null) }
    }

    private fun validateEmail() {
        val email = _uiState.value.emailText.trim()
        val ok = emailValidationUseCase(email)
        _uiState.update {
            it.copy(
                emailError = if (email.isNotEmpty() && !ok) "Enter a valid email address" else null,
                isEnabledInitPassword = ok
            )
        }
    }

    private fun updateOtpPasswordButtonState() {
        _uiState.update {
            it.copy(
                isEnabledUpdatePassword = it.otpString.length == OTP_LENGTH && it.newPassword.isNotBlank() &&
                        it.confirmPassword.isNotBlank() && it.newPasswordError == null && it.confirmPasswordError == null
            )
        }
    }

    private companion object {
        const val OTP_LENGTH = 6
        const val RESEND_SECONDS = 60
    }
}

data class ForgotPasswordUiState(
    val emailText: String = "",
    val emailError: String? = null,
    val isLoadingInitPassword: Boolean = false,
    val isEnabledInitPassword: Boolean = false,
    val isLoadingUpdatePassword: Boolean = false,
    val isEnabledUpdatePassword: Boolean = false,
    val passwordToken: String? = null,
    val newPassword: String = "",
    val confirmPassword: String = "",
    val newPasswordError: String? = null,
    val otpString: String = "",
    val isOtpValid: Boolean = false,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isReenteredPasswordVisible: Boolean = false,
    val showOtpView: Boolean = false,
    /** Form-level error (web AuthErrorAlert). */
    val formError: String? = null,
    /** Success/info line ("A 6-digit code has been sent…"). */
    val infoMessage: String? = null,
    /** Seconds until "Resend code" is available again. */
    val resendSecondsLeft: Int = 0,
    /** True when the reset response carried an access token and the session was stored. */
    val signedInDirectly: Boolean = false
)

sealed interface ForgotPasswordUiEvents {
    data class EmailChanged(val email: String) : ForgotPasswordUiEvents
    data object InitPassword : ForgotPasswordUiEvents
    data class OnOtpEntered(val otp: String) : ForgotPasswordUiEvents
    data class NewPasswordChanged(val password: String) : ForgotPasswordUiEvents
    data class ConfirmPasswordChanged(val password: String) : ForgotPasswordUiEvents
    data object TogglePasswordVisibility : ForgotPasswordUiEvents
    data object ToggleReenteredPasswordVisibility : ForgotPasswordUiEvents
    data object UpdatePassword : ForgotPasswordUiEvents
}
