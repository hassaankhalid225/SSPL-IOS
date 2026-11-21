package com.sspl.ui.kyc

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.SsplCheckboxRow
import androidx.compose.foundation.layout.Row
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PasswordField
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.ui.components.PhoneWithCountrySelector
import com.sspl.ui.components.countrypicker.Country
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sign in. Gradient hero + sheet (see [AuthScaffold]).
 *
 * Note: the phone/OTP sign-in mode is intentionally NOT exposed. The web has no phone-OTP login
 * endpoint and the app's OtpViewModel only checks a hard-coded code locally, so offering it would
 * let users into the shell without a session. The VM events are kept intact for when a real
 * endpoint exists ([showOtpToggle]).
 */
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.oneTimeEvents.collect { event ->
            when (event) {
                OneTimeEvents.NavigateToOtpScreen -> navController.navigate(Screen.OtpScreen.route)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.networkState.collect { result ->
            when (result) {
                is ApiStates.Failure -> snackbar.showSnackbar(
                    result.error.message.ifBlank { "Sign in failed. Check your connection and try again." }
                )
                is ApiStates.Success -> {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.SignInScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                else -> Unit
            }
        }
    }

    AuthScaffold(
        title = "Welcome back",
        subtitle = "Sign in to manage your SSPL membership, events and certificates",
        snackbarHostState = snackbar,
        modifier = modifier
    ) {
        LoginScreenContent(
            email = uiState.email,
            password = uiState.password,
            phone = uiState.phoneNo,
            selectedCountry = uiState.country,
            isOtpMode = uiState.isOtpMode,
            showPassword = uiState.isPasswordVisible,
            emailErrorMsg = uiState.emailError,
            passwordErrorMsg = uiState.passwordError,
            formError = uiState.formError,
            isLoginEnabled = uiState.isLoginEnabled,
            phoneErrorMsg = uiState.phoneNoError,
            isLoading = uiState.isLoading,
            isGuestLoading = uiState.isGuestLoading,
            rememberMe = uiState.rememberMe,
            showCountryChooser = uiState.showCountryChooser,
            onEvents = viewModel::onEvent,
            createNewAccount = { navController.navigate(Screen.SignUpScreen.route) },
            onForgotPassword = { navController.navigate(Screen.ForgotPasswordScreen.route) }
        )
    }
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    onEvents: (LoginScreenUiEvents) -> Unit = {},
    emailErrorMsg: String?,
    passwordErrorMsg: String?,
    formError: String? = null,
    isLoginEnabled: Boolean = false,
    phoneErrorMsg: String?,
    isLoading: Boolean = false,
    isGuestLoading: Boolean = false,
    rememberMe: Boolean = false,
    selectedCountry: Country,
    showCountryChooser: Boolean = false,
    email: String = "",
    phone: String = "",
    password: String = "",
    createNewAccount: () -> Unit,
    showPassword: Boolean = false,
    onForgotPassword: () -> Unit = {},
    isOtpMode: Boolean = false,
    showOtpToggle: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val busy = isLoading || isGuestLoading

    Column(modifier = modifier.fillMaxWidth()) {
        SheetHeading(title = "Sign in", caption = "Use the email and password of your SSPL account.")
        Spacer(Modifier.height(18.dp))

        AnimatedVisibility(visible = formError != null) {
            FeedbackBanner(
                message = formError.orEmpty(),
                isError = true,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        AnimatedVisibility(visible = !isOtpMode) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SsplTextField(
                    value = email,
                    onValueChange = { onEvents(LoginScreenUiEvents.OnEmailTextChange(it)) },
                    label = "Email address",
                    placeholder = "doctor@example.com",
                    leadingIcon = SsplIcons.AlternateEmail,
                    errorMessage = emailErrorMsg,
                    keyboardType = KeyboardType.Email,
                    enabled = !busy,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                PasswordField(
                    value = password,
                    onValueChange = { onEvents(LoginScreenUiEvents.OnPasswordTextChange(it)) },
                    label = "Password",
                    placeholder = "Enter your password",
                    isVisible = showPassword,
                    onToggleVisibility = { onEvents(LoginScreenUiEvents.TogglePasswordVisibility) },
                    errorMessage = passwordErrorMsg,
                    enabled = !busy,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isLoginEnabled && !busy) onEvents(LoginScreenUiEvents.OnSave)
                        }
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SsplCheckboxRow(
                        checked = rememberMe,
                        onCheckedChange = { onEvents(LoginScreenUiEvents.OnRememberMeChange(it)) },
                        label = "Remember me",
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    )
                    TextButtonLink(text = "Forgot password?", onClick = onForgotPassword, enabled = !busy)
                }
            }
        }

        AnimatedVisibility(visible = isOtpMode) {
            PhoneWithCountrySelector(
                selectedCountry = selectedCountry,
                errorMessage = phoneErrorMsg,
                phone = phone,
                onPhoneNumberChange = { onEvents(LoginScreenUiEvents.OnPhoneNoChange(it)) },
                showCountryChooser = showCountryChooser,
                onCountryChange = { onEvents(LoginScreenUiEvents.OnCountryChange(it)) },
                toggleCountryPicker = { onEvents(LoginScreenUiEvents.ToggleCountryChooser) }
            )
        }

        Spacer(Modifier.height(16.dp))

        PrimaryButton(
            text = if (isOtpMode) "Send code" else "Sign in",
            onClick = {
                focusManager.clearFocus()
                onEvents(LoginScreenUiEvents.OnSave)
            },
            loading = isLoading,
            enabled = isLoginEnabled && !isGuestLoading,
            trailingIcon = SsplIcons.Login
        )

        if (showOtpToggle) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButtonLink(
                    text = if (isOtpMode) "Sign in with email instead" else "Sign in with phone",
                    onClick = { onEvents(LoginScreenUiEvents.ToggleOtpMode) }
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        LabeledDivider("or")
        Spacer(Modifier.height(18.dp))

        OutlineButton(
            text = "Continue as guest",
            onClick = { onEvents(LoginScreenUiEvents.ContinueAsGuest) },
            icon = SsplIcons.PersonOutline,
            loading = isGuestLoading,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))
        AuthFooterLink(prompt = "New to SSPL?", action = "Create account", onClick = createNewAccount)
    }
}
