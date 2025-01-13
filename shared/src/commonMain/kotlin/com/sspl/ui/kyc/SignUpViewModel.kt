package com.sspl.ui.kyc

import androidx.lifecycle.ViewModel
import com.vanniktech.locale.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SignUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()


    fun onEvent(event: UserEvents) {
        when (event) {

            is UserEvents.SetEmail -> {
                _uiState.update { it.copy(email = event.email) }
            }

            is UserEvents.SetFirstName -> {
                _uiState.update { it.copy(firstName = event.firstName) }
            }

            is UserEvents.SetLastName -> {
                _uiState.update { it.copy(lastName = event.lastName) }
            }

            is UserEvents.SetPassword -> {
                _uiState.update { it.copy(password = event.password) }
            }

            is UserEvents.SetPhoneNo -> {
                _uiState.update { it.copy(phoneNo = event.phoneNo) }
            }

            UserEvents.Submit -> {
                handleSubmit()
            }

            is UserEvents.OnRetypePassword -> {
                _uiState.update { it.copy(retypedPassword = event.password) }
            }

            is UserEvents.OnCountryChange -> {
                updateCountry(event.country)
            }
            UserEvents.ToggleCountryChooser -> {
                toggleCountryChooser()
            }
            UserEvents.TogglePasswordVisibility -> {
                togglePasswordVisibility()
            }
        }
    }

    private fun handleSubmit() {
        val currentState = _uiState.value
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

    private fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }
}

data class UserUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNo: String = "",
    val retypedPassword: String = "", val country: Country = Country.PAKISTAN,
    val showCountryChooser: Boolean = false,
    val isPasswordVisible: Boolean = false

)

sealed interface UserEvents {
    data class SetFirstName(val firstName: String) : UserEvents
    data class SetLastName(val lastName: String) : UserEvents
    data class SetEmail(val email: String) : UserEvents
    data class SetPassword(val password: String) : UserEvents
    data class OnRetypePassword(val password: String) : UserEvents
    data class SetPhoneNo(val phoneNo: String) : UserEvents
    data class OnCountryChange(val country: Country) : UserEvents
    data object ToggleCountryChooser : UserEvents
    data object TogglePasswordVisibility : UserEvents
    data object Submit : UserEvents
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
