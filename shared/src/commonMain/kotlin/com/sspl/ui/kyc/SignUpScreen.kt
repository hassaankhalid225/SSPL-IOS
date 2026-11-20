package com.sspl.ui.kyc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.sspl.designsystem.components.PasswordField
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PhoneWithCountrySelector
import com.sspl.ui.components.countrypicker.Country
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sign up — step 1 of 2 (account). Step 2 is [com.sspl.ui.userdetails.UserDetailsScreen].
 */
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.networkState.collect { event ->
            when (event) {
                is ApiStates.Failure -> snackbar.showSnackbar(
                    event.error.message.ifBlank { "Couldn't create your account. Please try again." }
                )
                is ApiStates.Success -> navController.navigate(Screen.UserDetailsScreen.route) {
                    popUpTo(Screen.SignUpScreen.route) { inclusive = true }
                }
                else -> Unit
            }
        }
    }

    AuthScaffold(
        title = "Create your account",
        subtitle = "Join the Society of Surgeons Pakistan, Lahore",
        snackbarHostState = snackbar,
        onBack = { navController.popBackStack() },
        heroFraction = 0.32f
    ) {
        SignUpScreenContent(
            email = uiState.email,
            isSignUpEnabled = uiState.isSignUpEnabled,
            firstName = uiState.firstName,
            lastName = uiState.lastName,
            reEnterPassword = uiState.retypedPassword,
            reEnterPasswordErrorMsg = uiState.retypedPasswordError,
            isReEnteredVisible = uiState.isReenteredPasswordVisible,
            isLoading = uiState.isLoading,
            passwordErrorMsg = uiState.passwordError,
            firstNameErrorMsg = uiState.firstNameError,
            lastNameErrorMsg = uiState.lastNameError,
            phoneError = uiState.phoneNoError,
            password = uiState.password,
            isPasswordVisible = uiState.isPasswordVisible,
            showCountryChooser = uiState.showCountryChooser,
            country = uiState.country,
            emailErrorMsg = uiState.emailError,
            phoneNo = uiState.phoneNo,
            formError = uiState.formError,
            loginScreen = {
                if (!navController.popBackStack(Screen.SignInScreen.route, inclusive = false)) {
                    navController.navigate(Screen.SignInScreen.route)
                }
            },
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun SignUpScreenContent(
    modifier: Modifier = Modifier,
    email: String,
    isSignUpEnabled: Boolean,
    firstName: String,
    lastName: String,
    reEnterPassword: String,
    reEnterPasswordErrorMsg: String?,
    isReEnteredVisible: Boolean,
    isLoading: Boolean,
    passwordErrorMsg: String?,
    firstNameErrorMsg: String?,
    lastNameErrorMsg: String?,
    phoneError: String?,
    password: String,
    isPasswordVisible: Boolean,
    showCountryChooser: Boolean,
    country: Country,
    emailErrorMsg: String?,
    phoneNo: String,
    formError: String? = null,
    loginScreen: () -> Unit,
    onEvent: (UserEvents) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) },
        onDone = { focusManager.moveFocus(FocusDirection.Down) }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        StepIndicator(
            currentStep = 1,
            totalSteps = 2,
            stepLabels = listOf("Account", "Professional details")
        )
        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(visible = formError != null) {
            FeedbackBanner(
                message = formError.orEmpty(),
                isError = true,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormGroupLabel("Your name")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SsplTextField(
                    value = firstName,
                    onValueChange = { onEvent(UserEvents.SetFirstName(it)) },
                    label = "First name",
                    placeholder = "First name",
                    errorMessage = firstNameErrorMsg,
                    enabled = !isLoading,
                    keyboardActions = next,
                    modifier = Modifier.weight(1f)
                )
                SsplTextField(
                    value = lastName,
                    onValueChange = { onEvent(UserEvents.SetLastName(it)) },
                    label = "Last name",
                    placeholder = "Last name",
                    errorMessage = lastNameErrorMsg,
                    enabled = !isLoading,
                    keyboardActions = next,
                    modifier = Modifier.weight(1f)
                )
            }

            FormGroupLabel("Contact")
            SsplTextField(
                value = email,
                onValueChange = { onEvent(UserEvents.SetEmail(it)) },
                label = "Email address",
                placeholder = "doctor@example.com",
                leadingIcon = Icons.Default.AlternateEmail,
                errorMessage = emailErrorMsg,
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
                keyboardActions = next
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Mobile number", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
                PhoneWithCountrySelector(
                    selectedCountry = country,
                    phone = phoneNo,
                    errorMessage = phoneError,
                    onPhoneNumberChange = { onEvent(UserEvents.SetPhoneNo(it)) },
                    showCountryChooser = showCountryChooser,
                    onCountryChange = { onEvent(UserEvents.OnCountryChange(it)) },
                    toggleCountryPicker = { onEvent(UserEvents.ToggleCountryChooser) }
                )
            }

            FormGroupLabel("Security")
            PasswordField(
                value = password,
                onValueChange = { onEvent(UserEvents.SetPassword(it)) },
                label = "Create password",
                placeholder = "At least 6 characters",
                isVisible = isPasswordVisible,
                onToggleVisibility = { onEvent(UserEvents.TogglePasswordVisibility) },
                errorMessage = passwordErrorMsg,
                enabled = !isLoading,
                keyboardActions = next
            )
            PasswordField(
                value = reEnterPassword,
                onValueChange = { onEvent(UserEvents.OnRetypePassword(it)) },
                label = "Confirm password",
                placeholder = "Re-enter password",
                isVisible = isReEnteredVisible,
                onToggleVisibility = { onEvent(UserEvents.ToggleReenteredPasswordVisibility) },
                errorMessage = reEnterPasswordErrorMsg,
                enabled = !isLoading,
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onEvent(UserEvents.Submit)
                })
            )
        }

        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            text = "Continue",
            onClick = {
                focusManager.clearFocus()
                onEvent(UserEvents.Submit)
            },
            loading = isLoading,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Next you can add your professional details (PMDC, institution). You can skip it for now.",
            style = AppTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        AuthFooterLink(prompt = "Already have an account?", action = "Sign in", onClick = loginScreen)
    }
}

/** Small uppercase group label inside auth / profile forms. */
@Composable
fun FormGroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AppTheme.typography.overline,
        modifier = modifier.fillMaxWidth().padding(top = 4.dp)
    )
}
