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
            is UserEvents.SetEmail -> setEmail(event.email)
            is UserEvents.SetFirstName -> setFirstName(event.firstName)
            is UserEvents.SetLastName -> setLastName(event.lastName)
            is UserEvents.SetPassword -> setPassword(event.password)
            is UserEvents.SetPhoneNo -> setPhoneNo(event.phoneNo)
            is UserEvents.OnRetypePassword -> setRetypedPassword(event.password)
            is UserEvents.OnCountryChange -> updateCountry(event.country)
            UserEvents.ToggleCountryChooser -> toggleCountryChooser()
            UserEvents.TogglePasswordVisibility -> togglePasswordVisibility()
            UserEvents.ToggleReenteredPasswordVisibility -> toggleReenteredPasswordVisibility()
            UserEvents.Submit -> handleSubmit()
        }
    }

    private fun setEmail(email: String) {
        val isValid = emailValidationUseCase(email)
        _uiState.update {
            it.copy(
                email = email, emailError = if (!isValid) "Invalid email address" else null
            )
        }
        updateSignUpState()
    }

    private fun setFirstName(firstName: String) {
        val validationResult = nameValidationUseCase(firstName)
        _uiState.update {
            it.copy(
                firstName = firstName,
                firstNameError = if (validationResult is ValidationResult.Error) validationResult.message else null
            )
        }
        updateSignUpState()
    }

    private fun setLastName(lastName: String) {
        val validationResult = nameValidationUseCase(lastName)
        _uiState.update {
            it.copy(
                lastName = lastName,
                lastNameError = if (validationResult is ValidationResult.Error) validationResult.message else null
            )
        }
        updateSignUpState()
    }

    private fun setPassword(password: String) {
        val isValid = passwordValidationUseCase(password)
        _uiState.update {
            it.copy(
                password = password,
                passwordError = if (!isValid) "Password must be at least 6 characters long" else null
            )
        }
        updateSignUpState()
    }

    private fun setPhoneNo(phoneNo: String) {
        val isValid = phoneValidationUseCase(phoneNo)
        _uiState.update {
            it.copy(
                phoneNo = phoneNo,
                phoneNoError = if (!isValid) "Phone number must be between 10 to 15 digits" else null
            )
        }
        updateSignUpState()
    }

    private fun setRetypedPassword(retypedPassword: String) {
        _uiState.update {
            it.copy(
                retypedPassword = retypedPassword,
                retypedPasswordError = if (retypedPassword != it.password) "Passwords do not match" else null
            )
        }
        updateSignUpState()
    }

    private fun updateCountry(country: Country) {
        _uiState.update { it.copy(country = country, showCountryChooser = false) }
    }

    private fun toggleCountryChooser() {
        _uiState.update { it.copy(showCountryChooser = !_uiState.value.showCountryChooser) }
    }

    private fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !_uiState.value.isPasswordVisible) }
    }

    private fun toggleReenteredPasswordVisibility() {
        _uiState.update { it.copy(isReenteredPasswordVisible = !_uiState.value.isReenteredPasswordVisible) }
    }

    private fun handleSubmit() {
        if (_uiState.value.isSignUpEnabled) {
            _uiState.update { it.copy(isLoading = true) }
            // Sign-up logic here
//            if (responseOk){
//                _uiState.update {
//                    UserUiState()
//                }
//            }
        }
    }

    private fun updateSignUpState() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isSignUpEnabled = state.emailError == null && state.passwordError == null && state.phoneNoError == null && state.firstNameError == null && state.lastNameError == null && state.retypedPasswordError == null && state.email.isNotEmpty() && state.password.isNotEmpty() && state.phoneNo.isNotEmpty() && state.firstName.isNotEmpty() && state.lastName.isNotEmpty() && state.retypedPassword == state.password
            )
        }
    }
}


