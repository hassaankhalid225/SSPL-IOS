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
        viewModelScope.launch {
            when (event) {
                is LoginScreenUiEvents.OnEmailTextChange -> {
                    _uiState.update {
                        it.copy(email = event.emailText)
                    }
                }

                is LoginScreenUiEvents.OnPasswordTextChange -> {
                    _uiState.update {
                        it.copy(password = event.passwordText)
                    }
                }

                LoginScreenUiEvents.OnSave -> {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }

                    delay(1500)
                    if (_uiState.value.isOtpMode) {
                        val phoneNo = _uiState.value.country.callingCodes.first()
                            .toString() + _uiState.value.phoneNo


                        if (/*requestSentSuccessfull*/true){
                            _oneTimeEvents.emit(OneTimeEvents.NavigateToOtpScreen)
                        }else{
                            _oneTimeEvents.emit(OneTimeEvents.ShowError("Something went wrong"))
                        }
                    } else {
                        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
                        val isEmailValid = emailRegex.matches(_uiState.value.email)
                        val isPasswordValid = _uiState.value.password.length >= 6

                        if (!isEmailValid || !isPasswordValid) {
                            val errorMessage = when {
                                !isEmailValid -> "Please enter a valid email address."
                                !isPasswordValid -> "Password must be at least 10 characters long."
                                else -> "Invalid email or password."
                            }
                            _oneTimeEvents.emit(OneTimeEvents.ShowError(errorMessage))
                            _uiState.update {
                                it.copy(isLoading = false)
                            }
                            return@launch
                        }
                        if (/*aunthicated*/true){
                            _oneTimeEvents.emit(OneTimeEvents.LoginSuccess)
                        }else{
                            _oneTimeEvents.emit(OneTimeEvents.ShowError("Something went wrong"))
                        }
                    }
                    _uiState.update {
                        LoginUiState()
                    }
                }

                LoginScreenUiEvents.TogglePasswordVisibility -> {
                    _uiState.update {
                        it.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
                    }
                }

                LoginScreenUiEvents.ToggleOtpMode -> {
                    _uiState.update {
                        it.copy(isOtpMode = !_uiState.value.isOtpMode, phoneNo = "", email = "", password = "")
                    }
                }

                is LoginScreenUiEvents.OnPhoneNoChange -> {
                    _uiState.update {
                        it.copy(phoneNo = event.phoneNumber)

                    }
                }

                is LoginScreenUiEvents.OnCountryChange -> {
                    _uiState.update {
                        it.copy(country = event.country, showCountryChooser = false)
                    }
                }

                LoginScreenUiEvents.ToggleCountryChooser -> {
                    _uiState.update {
                        it.copy(showCountryChooser = !_uiState.value.showCountryChooser)
                    }
                }

            }
        }
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
