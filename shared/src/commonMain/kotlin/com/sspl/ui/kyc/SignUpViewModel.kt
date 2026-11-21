package com.sspl.ui.kyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.sspl.core.usecases.SignUpUserUseCase
import com.sspl.core.usecases.httpStatus
import com.sspl.core.usecases.ValidateUserNameUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sign up (step 1: account). Validation mirrors web `pages/auth/Signup.jsx`:
 * first/last name required, valid email, password >= 6 chars, valid phone.
 * The app additionally asks to confirm the password (kept from the previous app).
 */
class SignUpViewModel(
    private val emailValidationUseCase: EmailValidationUseCase,
    @Suppress("unused") private val passwordValidationUseCase: PasswordValidationUseCase,
    private val nameValidationUseCase: ValidateUserNameUseCase,
    private val phoneValidationUseCase: PhoneNumberValidationUseCase,
    private val signUpUserUseCase: SignUpUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val _networkState = MutableSharedFlow<ApiStates<Any>>()
    val networkState: SharedFlow<ApiStates<Any>> = _networkState.asSharedFlow()

    fun onEvent(event: UserEvents) {
        when (event) {
            is UserEvents.SetEmail -> _uiState.update {
                it.copy(email = event.email, emailError = emailError(event.email), formError = null)
            }

            is UserEvents.SetFirstName -> _uiState.update {
                it.copy(firstName = event.firstName, firstNameError = nameError(event.firstName, "First name"), formError = null)
            }

            is UserEvents.SetLastName -> _uiState.update {
                it.copy(lastName = event.lastName, lastNameError = nameError(event.lastName, "Last name"), formError = null)
            }

            is UserEvents.SetPassword -> _uiState.update {
                it.copy(
                    password = event.password,
                    passwordError = passwordError(event.password),
                    // keep the confirm field honest when the password changes after it was typed
                    retypedPasswordError = if (it.retypedPassword.isNotEmpty() && it.retypedPassword != event.password)
                        PASSWORDS_DONT_MATCH else null,
                    formError = null
                )
            }

            is UserEvents.SetPhoneNo -> _uiState.update {
                it.copy(
                    phoneNo = event.phoneNo,
                    phoneNoError = if (!phoneValidationUseCase(event.phoneNo)) "Invalid phone number" else null,
                    formError = null
                )
            }

            is UserEvents.OnRetypePassword -> _uiState.update {
                it.copy(
                    retypedPassword = event.password,
                    retypedPasswordError = if (event.password != it.password) PASSWORDS_DONT_MATCH else null
                )
            }

            is UserEvents.OnCountryChange -> _uiState.update { it.copy(country = event.country, showCountryChooser = false) }
            UserEvents.ToggleCountryChooser -> _uiState.update { it.copy(showCountryChooser = !it.showCountryChooser) }
            UserEvents.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            UserEvents.ToggleReenteredPasswordVisibility -> _uiState.update {
                it.copy(isReenteredPasswordVisible = !it.isReenteredPasswordVisible)
            }

            is UserEvents.SetGender -> _uiState.update { it.copy(gender = event.gender) }
            UserEvents.Submit -> submit()
        }
        updateSignUpState()
    }

    private fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !emailValidationUseCase(email.trim()) -> "Invalid email format"
        else -> null
    }

    private fun nameError(value: String, field: String): String? = when {
        value.isBlank() -> "$field is required"
        nameValidationUseCase(value) is ValidationResult.Error -> "$field cannot contain numbers"
        else -> null
    }

    private fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD -> "Password must be at least $MIN_PASSWORD characters"
        else -> null
    }

    private fun submit() {
        val s = _uiState.value
        if (s.isLoading) return
        // Validate everything on submit so untouched fields show their errors (web behaviour).
        _uiState.update {
            it.copy(
                firstNameError = nameError(it.firstName, "First name"),
                lastNameError = nameError(it.lastName, "Last name"),
                emailError = emailError(it.email),
                passwordError = passwordError(it.password),
                phoneNoError = if (!phoneValidationUseCase(it.phoneNo)) "Invalid phone number" else null,
                retypedPasswordError = if (it.retypedPassword != it.password) PASSWORDS_DONT_MATCH else null
            )
        }
        updateSignUpState()
        if (!_uiState.value.isSignUpEnabled) return

        viewModelScope.launch {
            val st = _uiState.value
            signUpUserUseCase.signUpUser(
                firstName = st.firstName,
                lastName = st.lastName,
                email = st.email,
                password = st.password,
                phoneNumber = st.country.dialCode + st.phoneNo,
                gender = st.gender,
                prefix = st.prefix
            ).collect { state ->
                var isHandled = false
                if (state is ApiStates.Failure) {
                    // Same conflict detection as web Signup.jsx
                    val msg = state.error.message.lowercase()
                    val isConflict = state.error.httpStatus() == 409 || msg.contains("already exist") || msg.contains("already registered") ||
                            msg.contains("already in use") || msg.contains("already taken") ||
                            msg.contains("duplicate") || msg.contains("conflict") ||
                            ((msg.contains("email") || msg.contains("phone") || msg.contains("mobile")) &&
                                    (msg.contains("registered") || msg.contains("exist") || msg.contains("taken")))
                    if (isConflict) {
                        if (msg.contains("phone") || msg.contains("mobile")) {
                            _uiState.update {
                                it.copy(
                                    phoneNoError = "Phone number is already in use.",
                                    formError = "This phone number is already registered with an account.",
                                    isLoading = false
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    emailError = "An account with this email already exists.",
                                    formError = "An account with this email already exists. Please sign in to continue.",
                                    isLoading = false
                                )
                            }
                        }
                        isHandled = true
                    }
                }

                if (isHandled) {
                    updateSignUpState()
                } else {
                    _uiState.update { it.copy(isLoading = state is ApiStates.Loading) }
                    _networkState.emit(state)
                }
            }
        }
    }

    private fun updateSignUpState() {
        _uiState.update { s ->
            s.copy(
                isSignUpEnabled = s.emailError == null && s.passwordError == null && s.phoneNoError == null &&
                        s.firstNameError == null && s.lastNameError == null && s.retypedPasswordError == null &&
                        s.email.isNotBlank() && s.password.isNotEmpty() && s.phoneNo.isNotEmpty() &&
                        s.firstName.isNotBlank() && s.lastName.isNotBlank() && s.retypedPassword == s.password
            )
        }
    }

    private companion object {
        /** Web sign-up minimum (Signup.jsx). Reset password uses 8 like the web reset page. */
        const val MIN_PASSWORD = 6
        const val PASSWORDS_DONT_MATCH = "Passwords do not match"
    }
}
