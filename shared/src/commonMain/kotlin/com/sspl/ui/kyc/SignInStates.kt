package com.sspl.ui.kyc

import com.sspl.ui.components.countrypicker.Country
import com.sspl.ui.components.countrypicker.Pakistan


sealed interface LoginScreenUiEvents {
    data class OnEmailTextChange(val emailText: String) : LoginScreenUiEvents
    data class OnPasswordTextChange(val passwordText: String) : LoginScreenUiEvents
    data class OnPhoneNoChange(val phoneNumber: String) : LoginScreenUiEvents
    data object OnSave : LoginScreenUiEvents
    data object ContinueAsGuest : LoginScreenUiEvents
    data object TogglePasswordVisibility : LoginScreenUiEvents
    data class OnRememberMeChange(val checked: Boolean) : LoginScreenUiEvents
    data object ToggleOtpMode : LoginScreenUiEvents
     data class OnCountryChange(val country: Country) : LoginScreenUiEvents
    data object ToggleCountryChooser : LoginScreenUiEvents
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isOtpMode: Boolean = false,
    val isLoginEnabled: Boolean = false,
    val phoneNo: String = "",
    val isLoading: Boolean = false,
    /** True while "Continue as guest" is in flight (kept separate so only that button shows dots). */
    val isGuestLoading: Boolean = false,
    /** Form-level error shown above the actions (mirrors the web AuthErrorAlert). */
    val formError: String? = null,
    val country: Country = Pakistan,
    val showCountryChooser: Boolean = false,
    val isPasswordVisible: Boolean = false,
    /** Sent as `rememberMe` to POST /auth/login (web Login.jsx). */
    val rememberMe: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneNoError: String? = null
)

sealed interface OneTimeEvents {
    data object NavigateToOtpScreen : OneTimeEvents
}
