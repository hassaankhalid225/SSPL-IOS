package com.sspl.ui.kyc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.designsystem.components.PasswordField
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StepIndicator
import com.sspl.ui.components.countrypicker.Country
import com.sspl.designsystem.theme.AppTheme
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import com.sspl.ui.components.PhoneWithCountrySelector
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A3: Multi-step Sign Up wizard — Step 1: Account & Credentials.
 */
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.networkState.collect { event ->
            when (event) {
                is ApiStates.Failure -> {
                    scaffoldState.showSnackbar(event.error.message)
                }
                is ApiStates.Success -> {
                    navController.navigate(Screen.UserDetailsScreen.route) {
                        popUpTo(Screen.SignUpScreen.route) {
                            inclusive = true
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        containerColor = AppTheme.colors.background
    ) { padding ->
        SignUpScreenContent(
            modifier = Modifier.padding(padding),
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
            loginScreen = {
                navController.navigate(Screen.SignInScreen.route)
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
    loginScreen: () -> Unit,
    onEvent: (UserEvents) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Logo & Header
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "SSPL Logo",
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create Account",
                style = AppTheme.typography.headlineMedium.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Join Society of Surgeons Pakistan (Lahore)",
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.muted
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator
            StepIndicator(
                currentStep = 1,
                totalSteps = 2,
                stepLabels = listOf("Account Info", "Professional KYC")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fields Card
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SsplTextField(
                                value = firstName,
                                onValueChange = { onEvent(UserEvents.SetFirstName(it)) },
                                label = "First Name",
                                placeholder = "First name",
                                leadingIcon = Icons.Default.Person,
                                errorMessage = firstNameErrorMsg,
                                isError = firstNameErrorMsg != null
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SsplTextField(
                                value = lastName,
                                onValueChange = { onEvent(UserEvents.SetLastName(it)) },
                                label = "Last Name",
                                placeholder = "Last name",
                                errorMessage = lastNameErrorMsg,
                                isError = lastNameErrorMsg != null
                            )
                        }
                    }

                    PhoneWithCountrySelector(
                        selectedCountry = country,
                        phone = phoneNo,
                        errorMessage = phoneError,
                        onPhoneNumberChange = { onEvent(UserEvents.SetPhoneNo(it)) },
                        showCountryChooser = showCountryChooser,
                        onCountryChange = { onEvent(UserEvents.OnCountryChange(it)) },
                        toggleCountryPicker = { onEvent(UserEvents.ToggleCountryChooser) }
                    )

                    SsplTextField(
                        value = email,
                        onValueChange = { onEvent(UserEvents.SetEmail(it)) },
                        label = "Email Address",
                        placeholder = "doctor@example.com",
                        leadingIcon = Icons.Default.Email,
                        errorMessage = emailErrorMsg,
                        isError = emailErrorMsg != null
                    )

                    PasswordField(
                        value = password,
                        onValueChange = { onEvent(UserEvents.SetPassword(it)) },
                        label = "Create Password",
                        placeholder = "At least 8 characters",
                        isVisible = isPasswordVisible,
                        onToggleVisibility = { onEvent(UserEvents.TogglePasswordVisibility) },
                        errorMessage = passwordErrorMsg,
                        isError = passwordErrorMsg != null
                    )

                    PasswordField(
                        value = reEnterPassword,
                        onValueChange = { onEvent(UserEvents.OnRetypePassword(it)) },
                        label = "Confirm Password",
                        placeholder = "Re-enter password",
                        isVisible = isReEnteredVisible,
                        onToggleVisibility = { onEvent(UserEvents.ToggleReenteredPasswordVisibility) },
                        errorMessage = reEnterPasswordErrorMsg,
                        isError = reEnterPasswordErrorMsg != null
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrimaryButton(
                        text = "Next: Professional Details",
                        onClick = { onEvent(UserEvents.Submit) },
                        isLoading = isLoading,
                        enabled = isSignUpEnabled && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                )
                Text(
                    text = "Sign In",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { loginScreen() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
