package com.sspl.ui.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.UserResponse
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.LoginUserUseCase
import com.sspl.core.usecases.httpStatus
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.sspl.core.usecases.SignUpUserUseCase
import com.sspl.utils.EMAIL_GUEST_USER
import com.sspl.utils.PASSWORD_GUEST_USER
import com.sspl.utils.PHONE_GUEST_USER
import com.sspl.core.push.PushNotificationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.sspl.utils.DateTimeUtils

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 09/01/2025.
 * se.muhammadimran@gmail.com
 */
class SignInViewModel(
    private val emailValidationUseCase: EmailValidationUseCase,
    private val phoneNumberValidationUseCase: PhoneNumberValidationUseCase,
    private val passwordValidationUseCase: PasswordValidationUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val signUpUseCase: SignUpUserUseCase,
    private val pushNotificationService: PushNotificationService,
    private val userSession: com.sspl.session.UserSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _oneTimeEvents = MutableSharedFlow<OneTimeEvents>()
    val oneTimeEvents = _oneTimeEvents.asSharedFlow()


    private val _networkState = MutableSharedFlow<ApiStates<UserResponse>>()
    val networkState: SharedFlow<ApiStates<UserResponse>> = _networkState.asSharedFlow()


    fun onEvent(event: LoginScreenUiEvents) {
        when (event) {
            is LoginScreenUiEvents.OnEmailTextChange -> {
                _uiState.update { it.copy(email = event.emailText, emailError = null, formError = null) }
                validateEmail()
            }

            is LoginScreenUiEvents.OnPasswordTextChange -> {
                _uiState.update { it.copy(password = event.passwordText, passwordError = null, formError = null) }
                validatePassword()
            }

            LoginScreenUiEvents.OnSave -> handleSave()
            LoginScreenUiEvents.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            LoginScreenUiEvents.ToggleOtpMode -> _uiState.update {
                it.copy(
                    isOtpMode = !it.isOtpMode,
                    phoneNo = "",
                    email = "",
                    isLoginEnabled = false,
                    password = "",
                    emailError = null,
                    passwordError = null,
                    formError = null
                )
            }

            is LoginScreenUiEvents.OnPhoneNoChange -> {
                _uiState.update { it.copy(phoneNo = event.phoneNumber) }
                validatePhoneNumber()
            }

            is LoginScreenUiEvents.OnCountryChange -> _uiState.update {
                it.copy(country = event.country, showCountryChooser = false)
            }

            LoginScreenUiEvents.ToggleCountryChooser -> _uiState.update {
                it.copy(showCountryChooser = !it.showCountryChooser)
            }

            LoginScreenUiEvents.ContinueAsGuest -> continueAsGuest()
            is LoginScreenUiEvents.OnRememberMeChange -> _uiState.update { it.copy(rememberMe = event.checked) }
        }
    }

    /*
     * Validation mirrors the web sign-in form (sspl-frotend/src/pages/auth/Login.jsx):
     * both fields are required and the email must be a valid address. The web does NOT
     * enforce a minimum password length on sign-in (only on sign-up / reset), so neither do we -
     * otherwise members with older short passwords could not sign in from the app.
     */
    private fun isPasswordPresent(password: String) = password.isNotEmpty()

    private fun validatePassword() {
        val ok = isPasswordPresent(_uiState.value.password)
        _uiState.update {
            it.copy(
                passwordError = if (!ok) PASSWORD_REQUIRED else null,
                isLoginEnabled = ok && it.emailError == null && it.email.isNotBlank()
            )
        }
    }

    private fun validateEmail() {
        val email = _uiState.value.email.trim()
        val ok = emailValidationUseCase(email)
        _uiState.update {
            it.copy(
                emailError = when {
                    email.isEmpty() -> EMAIL_REQUIRED
                    !ok -> EMAIL_INVALID
                    else -> null
                },
                isLoginEnabled = ok && it.passwordError == null && isPasswordPresent(it.password)
            )
        }
    }

    private fun validatePhoneNumber() {
        val phoneNo = "${_uiState.value.country.dialCode.first()}${_uiState.value.phoneNo}"
        val isPhoneValid = phoneNumberValidationUseCase(phoneNo)
        _uiState.update {
            it.copy(
                phoneNoError = if (!isPhoneValid) "Invalid phone number" else null,
                isLoginEnabled = isPhoneValid && it.isOtpMode
            )
        }
    }

    private fun handleSave() {
        if (_uiState.value.isLoading || _uiState.value.isGuestLoading) return
        viewModelScope.launch {
            if (_uiState.value.isOtpMode) handleOtpSave() else handleEmailPasswordSave()
        }
    }

    private suspend fun handleOtpSave() {
        val phoneNo = "${_uiState.value.country.dialCode.first()}${_uiState.value.phoneNo}"
        if (phoneNumberValidationUseCase(phoneNo)) {
            _oneTimeEvents.emit(OneTimeEvents.NavigateToOtpScreen)
        } else {
            _uiState.update { it.copy(phoneNoError = "Invalid phone number") }
        }
    }

    private suspend fun handleEmailPasswordSave() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val isEmailValid = emailValidationUseCase(email)
        val isPasswordValid = isPasswordPresent(password)

        if (!isEmailValid || !isPasswordValid) {
            _uiState.update {
                it.copy(
                    emailError = if (!isEmailValid) (if (email.isEmpty()) EMAIL_REQUIRED else EMAIL_INVALID) else it.emailError,
                    passwordError = if (!isPasswordValid) PASSWORD_REQUIRED else it.passwordError
                )
            }
            return
        }
        logInViaEmailPassword(email = email, password = password)
    }

    private suspend fun logInViaEmailPassword(email: String, password: String) {
        loginUserUseCase.signInUserViaEmail(
            email = email,
            password = password,
            rememberMe = _uiState.value.rememberMe
        ).collect { state ->
            _uiState.update { it.copy(isLoading = state is ApiStates.Loading) }

            var isHandled = false
            if (state is ApiStates.Failure) {
                // Same classification as the web Login.jsx error handler.
                // HTTP status first (404 = no account, 401 = bad credentials), then message text.
                val status = state.error.httpStatus()
                val msg = state.error.message.trim().lowercase()
                val isNotFound = status == 404 || msg == "not found" ||
                        msg.contains("user not found") || msg.contains("account not found") ||
                        msg.contains("does not exist") || msg.contains("no user") ||
                        msg.contains("no account") || msg.contains("not registered")
                val isInvalidCreds = status == 401 || msg.contains("unauthorized") ||
                        msg.contains("invalid credential") || msg.contains("invalid password") ||
                        msg.contains("wrong password")
                when {
                    isNotFound -> {
                        _uiState.update {
                            it.copy(
                                emailError = "Account does not exist with this email.",
                                formError = "Account does not exist with this email. Please create an account to continue."
                            )
                        }
                        isHandled = true
                    }
                    isInvalidCreds -> {
                        _uiState.update {
                            it.copy(
                                passwordError = "Check your password or use password recovery.",
                                formError = "Invalid email or password. Please check your credentials and try again."
                            )
                        }
                        isHandled = true
                    }
                }
            }

            if (!isHandled) {
                // Note: registerDeviceToken() is already called in LoginUserUseCase after successful login
                _networkState.emit(state)
            }
        }
    }

    private fun continueAsGuest() {
        if (_uiState.value.isLoading || _uiState.value.isGuestLoading) return
        viewModelScope.launch {
            // Reuse an existing guest session instead of registering a new guest account each time.
            val cachedUser = userSession.userFlow.value
            val cachedToken = userSession.token
            if (cachedUser?.isGuest() == true && !cachedToken.isNullOrBlank()) {
                _networkState.emit(ApiStates.Success(UserResponse(accessToken = cachedToken, user = cachedUser)))
                return@launch
            }
            val email = "${DateTimeUtils.timeInMilliNow()}." + EMAIL_GUEST_USER
            signUpUseCase.signUpUser(
                firstName = "Guest",
                lastName = "User",
                email = email,
                password = PASSWORD_GUEST_USER,
                phoneNumber = PHONE_GUEST_USER
            ).collect { state ->
                _uiState.update { it.copy(isGuestLoading = state is ApiStates.Loading) }
                // Note: registerDeviceToken() is already called in SignUpUserUseCase after successful signup
                _networkState.emit(state)
            }
        }
    }

    private companion object {
        const val EMAIL_REQUIRED = "Email is required"
        const val EMAIL_INVALID = "Enter a valid email address"
        const val PASSWORD_REQUIRED = "Password is required"
    }
}
