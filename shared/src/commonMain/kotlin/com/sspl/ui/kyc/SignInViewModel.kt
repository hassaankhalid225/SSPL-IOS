package com.sspl.ui.kyc


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SignInViewModel : ViewModel() {
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
            delay(1500)

            if (_uiState.value.isOtpMode) {
                handleOtpSave()
            } else {
                handleEmailPasswordSave()
            }

            _uiState.update { LoginUiState() }
        }
    }

    private suspend fun handleOtpSave() {
        val phoneNo = "${_uiState.value.country.callingCodes.first()}${_uiState.value.phoneNo}"
        if (/*requestSentSuccessful*/true) {
            _oneTimeEvents.emit(OneTimeEvents.NavigateToOtpScreen)
        } else {
            _oneTimeEvents.emit(OneTimeEvents.ShowError("Something went wrong"))
        }
    }

    private suspend fun handleEmailPasswordSave() {
        val (isEmailValid, isPasswordValid) = validateEmailAndPassword()
        if (!isEmailValid || !isPasswordValid) {
            emitValidationError(isEmailValid, isPasswordValid)
            return
        }

        if (/*authenticated*/true) {
            _oneTimeEvents.emit(OneTimeEvents.LoginSuccess)
        } else {
            _oneTimeEvents.emit(OneTimeEvents.ShowError("Something went wrong"))
        }
    }

    private fun validateEmailAndPassword(): Pair<Boolean, Boolean> {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        val isEmailValid = emailRegex.matches(_uiState.value.email)
        val isPasswordValid = _uiState.value.password.length >= 6
        return Pair(isEmailValid, isPasswordValid)
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
    val country: Country = Country.PAKISTAN,
    val isOtpMode: Boolean = false,
    val phoneNo: String = "",
    val showCountryChooser: Boolean = false,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false
)

sealed interface LoginScreenUiEvents {
    data class OnEmailTextChange(val emailText: String) : LoginScreenUiEvents
    data class OnPasswordTextChange(val passwordText: String) : LoginScreenUiEvents
    data class OnPhoneNoChange(val phoneNumber: String) : LoginScreenUiEvents
    data class OnCountryChange(val country: Country) : LoginScreenUiEvents
    data object OnSave : LoginScreenUiEvents
    data object TogglePasswordVisibility : LoginScreenUiEvents
    data object ToggleOtpMode : LoginScreenUiEvents
    data object ToggleCountryChooser : LoginScreenUiEvents
}

sealed interface OneTimeEvents{
data object NavigateToOtpScreen:OneTimeEvents
    data object LoginSuccess:OneTimeEvents
    data class ShowError(val message:String):OneTimeEvents
}
