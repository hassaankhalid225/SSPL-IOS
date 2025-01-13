package com.sspl.ui.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.vanniktech.locale.Country
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 09/01/2025.
 * se.muhammadimran@gmail.com
 */
class SignInViewModel(
    private val emailValidationUseCase: EmailValidationUseCase,
    private val phoneNumberValidationUseCase: PhoneNumberValidationUseCase,
    private val passwordValidationUseCase: PasswordValidationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _oneTimeEvents = MutableSharedFlow<OneTimeEvents>()
    val oneTimeEvents = _oneTimeEvents.asSharedFlow()

    fun onEvent(event: LoginScreenUiEvents) {
        when (event) {
            is LoginScreenUiEvents.OnEmailTextChange -> updateEmail(event.emailText)
            is LoginScreenUiEvents.OnPasswordTextChange -> updatePassword(event.passwordText)
            LoginScreenUiEvents.OnSave -> handleSave()
            LoginScreenUiEvents.TogglePasswordVisibility -> togglePasswordVisibility()
            LoginScreenUiEvents.ToggleOtpMode -> toggleOtpMode()
            is LoginScreenUiEvents.OnPhoneNoChange -> updatePhoneNumber(event.phoneNumber)
            is LoginScreenUiEvents.OnCountryChange -> updateCountry(event.country)
            LoginScreenUiEvents.ToggleCountryChooser -> toggleCountryChooser()
            LoginScreenUiEvents.OnEmailFocusLost -> validateEmail()
            LoginScreenUiEvents.OnPasswordFocusLost -> validatePassword()
            LoginScreenUiEvents.OnPhoneNoFocusLost -> validatePhoneNo()
        }
    }

    private fun validateEmail() {
        val isValid = emailValidationUseCase(_uiState.value.email)
        _uiState.update {
            it.copy(emailError = if (isValid) null else "Please enter a valid email address.")
        }
    }

    private fun validatePassword() {
        val isValid = passwordValidationUseCase(_uiState.value.password)
        _uiState.update {
            it.copy(passwordError = if (isValid) null else "Password must be at least 6 characters long.")
        }
    }

    private fun validatePhoneNo() {
        val phoneNo = "${_uiState.value.country.callingCodes.first()}${_uiState.value.phoneNo}"
        val isValid = phoneNumberValidationUseCase(phoneNo)
        _uiState.update {
            it.copy(phoneNoError = if (isValid) null else "Please enter a valid phone number.")
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    private fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    private fun handleSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500) // Simulate loading time
            if (_uiState.value.isOtpMode) {
                handleOtpSave()
            } else {
                handleEmailPasswordSave()
            }
        }
    }

    private suspend fun handleOtpSave() {
        val phoneNo = "${_uiState.value.country.callingCodes.first()}${_uiState.value.phoneNo}"
        if (phoneNumberValidationUseCase(phoneNo)) {
            _oneTimeEvents.emit(OneTimeEvents.NavigateToOtpScreen)
        } else {
            _oneTimeEvents.emit(OneTimeEvents.ShowError("Invalid phone number"))
        }
    }

    private suspend fun handleEmailPasswordSave() {
        val isEmailValid = emailValidationUseCase(_uiState.value.email)
        val isPasswordValid = passwordValidationUseCase(_uiState.value.password)

        if (!isEmailValid || !isPasswordValid) {
            emitValidationError(isEmailValid, isPasswordValid)
            return
        }
        val isAuthenticated = true // For now, assume success
        if (isAuthenticated) {
            _oneTimeEvents.emit(OneTimeEvents.LoginSuccess)
        } else {
            _oneTimeEvents.emit(OneTimeEvents.ShowError("Something went wrong"))
        }
    }

    private suspend fun emitValidationError(isEmailValid: Boolean, isPasswordValid: Boolean) {
        val errorMessage = when {
            !isEmailValid -> "Please enter a valid email address."
            !isPasswordValid -> "Password must be at least 6 characters long."
            else -> "Invalid email or password."
        }
        _oneTimeEvents.emit(OneTimeEvents.ShowError(errorMessage))
        _uiState.update { it.copy(isLoading = false) }
    }

    private fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun toggleOtpMode() {
        _uiState.update {
            it.copy(
                isOtpMode = !it.isOtpMode,
                phoneNo = "",
                email = "",
                password = ""
            )
        }
    }

    private fun updatePhoneNumber(phoneNumber: String) {
        _uiState.update { it.copy(phoneNo = phoneNumber) }
    }

    private fun updateCountry(country: Country) {
        _uiState.update {
            it.copy(
                country = country,
                showCountryChooser = false
            )
        }
    }

    private fun toggleCountryChooser() {
        _uiState.update { it.copy(showCountryChooser = !it.showCountryChooser) }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isOtpMode: Boolean = false,
    val phoneNo: String = "",
    val isLoading: Boolean = false,
    val country: Country = Country.PAKISTAN,
    val showCountryChooser: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneNoError: String? = null
)

sealed interface LoginScreenUiEvents {
    data class OnEmailTextChange(val emailText: String) : LoginScreenUiEvents
    data class OnPasswordTextChange(val passwordText: String) : LoginScreenUiEvents
    data class OnPhoneNoChange(val phoneNumber: String) : LoginScreenUiEvents
    data object OnSave : LoginScreenUiEvents
    data object TogglePasswordVisibility : LoginScreenUiEvents
    data object ToggleOtpMode : LoginScreenUiEvents
    data class OnCountryChange(val country: Country) : LoginScreenUiEvents
    data object ToggleCountryChooser : LoginScreenUiEvents
    data object OnEmailFocusLost : LoginScreenUiEvents
    data object OnPasswordFocusLost : LoginScreenUiEvents
    data object OnPhoneNoFocusLost : LoginScreenUiEvents
}

sealed interface OneTimeEvents {
    data object NavigateToOtpScreen : OneTimeEvents
    data object LoginSuccess : OneTimeEvents
    data class ShowError(val message: String) : OneTimeEvents
}
