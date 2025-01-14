package com.sspl.ui.kyc

import androidx.lifecycle.ViewModel
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.sspl.core.usecases.ValidateUserFirstNameUseCase
import com.vanniktech.locale.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SignUpViewModel(
    private val emailValidationUseCase: EmailValidationUseCase,
    private val passwordValidationUseCase: PasswordValidationUseCase,
    private val nameValidationUseCase: ValidateUserFirstNameUseCase,
    private val phoneValidationUseCase: PhoneNumberValidationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun onEvent(event: UserEvents) {
        when (event) {
            is UserEvents.SetEmail -> {
                val isValid = emailValidationUseCase(event.email)
                _uiState.update {
                    it.copy(
                        email = event.email,
                        emailError = if (!isValid) "Invalid email address" else null
                    )
                }
            }
            is UserEvents.SetFirstName -> {
                val validationResult = nameValidationUseCase(event.firstName)
                _uiState.update {
                    it.copy(
                        firstName = event.firstName,
                        firstNameError = if (validationResult is ValidationResult.Error) validationResult.message else null
                    )
                }
            }
            is UserEvents.SetLastName -> {
                val validationResult = nameValidationUseCase(event.lastName)
                _uiState.update {
                    it.copy(
                        lastName = event.lastName,
                        lastNameError = if (validationResult is ValidationResult.Error) validationResult.message else null
                    )
                }
            }
            is UserEvents.SetPassword -> {
                val isValid = passwordValidationUseCase(event.password)
                _uiState.update {
                    it.copy(
                        password = event.password,
                        passwordError = if (!isValid) "Password must be at least 6 characters long" else null
                    )
                }
            }
            is UserEvents.SetPhoneNo -> {
                val isValid = phoneValidationUseCase(event.phoneNo)
                _uiState.update {
                    it.copy(
                        phoneNo = event.phoneNo,
                        phoneNoError = if (!isValid) "Phone number must be between 10 to 15 digits" else null
                    )
                }
            }
            is UserEvents.OnRetypePassword -> {
                _uiState.update {
                    it.copy(
                        retypedPassword = event.password,
                        retypedPasswordError = if (event.password != _uiState.value.password) "Passwords do not match" else null
                    )
                }
            }
            is UserEvents.OnCountryChange -> {
                _uiState.update { it.copy(country = event.country, showCountryChooser = false) }
            }
            UserEvents.ToggleCountryChooser -> {
                _uiState.update { it.copy(showCountryChooser = !_uiState.value.showCountryChooser) }
            }
            UserEvents.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !_uiState.value.isPasswordVisible) }
            }
            UserEvents.ToggleReenteredPasswordVisibility -> {
                _uiState.update { it.copy(isReenteredPasswordVisible = !_uiState.value.isReenteredPasswordVisible) }
            }
            UserEvents.Submit -> {
                if (_uiState.value.isSignUpEnabled) {
                    _uiState.update { it.copy(isLoading = true) }
                    // Sign-up logic here
                }
            }
        }
        updateSignUpState()
    }

    private fun updateSignUpState() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isSignUpEnabled = state.emailError == null &&
                        state.passwordError == null &&
                        state.phoneNoError == null &&
                        state.firstNameError == null &&
                        state.lastNameError == null &&
                        state.retypedPasswordError == null &&
                        state.email.isNotEmpty() &&
                        state.password.isNotEmpty() &&
                        state.phoneNo.isNotEmpty() &&
                        state.firstName.isNotEmpty() &&
                        state.lastName.isNotEmpty() &&
                        state.retypedPassword == state.password
            )
        }
    }
}

