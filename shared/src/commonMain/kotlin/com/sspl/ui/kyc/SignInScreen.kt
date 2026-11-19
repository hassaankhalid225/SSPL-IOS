package com.sspl.ui.kyc

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.PasswordField
import com.sspl.ui.components.PhoneWithCountrySelector
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.ui.components.countrypicker.Country
import com.sspl.designsystem.theme.AppTheme
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A2: Modernized Login Screen with Guest Mode and Design System integration.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.oneTimeEvents.collect { event ->
            when (event) {
                OneTimeEvents.NavigateToOtpScreen -> {
                    navController.navigate(Screen.OtpScreen.route)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.networkState.collect { result ->
            when (result) {
                is ApiStates.Failure -> scaffoldState.showSnackbar(result.error.message)
                is ApiStates.Success -> {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.SignInScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
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
        LoginScreenContent(
            email = uiState.email,
            password = uiState.password,
            phone = uiState.phoneNo,
            selectedCountry = uiState.country,
            isOtpMode = uiState.isOtpMode,
            showPassword = uiState.isPasswordVisible,
            emailErrorMsg = uiState.emailError,
            passwordErrorMsg = uiState.passwordError,
            isLoginEnabled = uiState.isLoginEnabled,
            phoneErrorMsg = uiState.phoneNoError,
            isLoading = uiState.isLoading,
            showCountryChooser = uiState.showCountryChooser,
            onEvents = viewModel::onEvent,
            createNewAccount = {
                navController.navigate(Screen.SignUpScreen.route)
            },
            onForgotPassword = {
                navController.navigate(Screen.ForgotPasswordScreen.route)
            },
            modifier = modifier.padding(padding)
        )
    }
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    onEvents: (LoginScreenUiEvents) -> Unit = {},
    emailErrorMsg: String?,
    passwordErrorMsg: String?,
    isLoginEnabled: Boolean = false,
    phoneErrorMsg: String?,
    isLoading: Boolean = false,
    selectedCountry: Country,
    showCountryChooser: Boolean = false,
    email: String = "",
    phone: String = "",
    password: String = "",
    createNewAccount: () -> Unit,
    showPassword: Boolean = false,
    onForgotPassword: () -> Unit = {},
    isOtpMode: Boolean = false
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // SSPL Logo & Header
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "SSPL Logo",
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                style = AppTheme.typography.headlineMedium.copy(
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to access your SSPL portal & membership",
                style = AppTheme.typography.bodyMedium.copy(
                    color = AppTheme.colors.muted
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Input Card
            SsplCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Form Section
                AnimatedVisibility(visible = !isOtpMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SsplTextField(
                            value = email,
                            onValueChange = { onEvents(LoginScreenUiEvents.OnEmailTextChange(it)) },
                            label = "Email Address",
                            placeholder = "doctor@example.com",
                            leadingIcon = Icons.Default.Email,
                            errorMessage = emailErrorMsg,
                            isError = emailErrorMsg != null
                        )

                        PasswordField(
                            value = password,
                            onValueChange = { onEvents(LoginScreenUiEvents.OnPasswordTextChange(it)) },
                            label = "Password",
                            placeholder = "Enter your password",
                            isVisible = showPassword,
                            onToggleVisibility = { onEvents(LoginScreenUiEvents.TogglePasswordVisibility) },
                            errorMessage = passwordErrorMsg,
                            isError = passwordErrorMsg != null
                        )

                        // Forgot password link
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TextButtonLink(
                                text = "Forgot Password?",
                                onClick = onForgotPassword
                            )
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

                Spacer(modifier = Modifier.height(18.dp))

                // Sign In Button
                PrimaryButton(
                    text = if (isOtpMode) "Send OTP Code" else "Sign In",
                    onClick = { onEvents(LoginScreenUiEvents.OnSave) },
                    isLoading = isLoading,
                    enabled = isLoginEnabled && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Toggle Email / Phone login mode
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButtonLink(
                        text = if (isOtpMode) "Sign in with Email & Password" else "Sign in via Phone OTP",
                        onClick = { onEvents(LoginScreenUiEvents.ToggleOtpMode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider with "OR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = AppTheme.colors.border
                )
                Text(
                    text = "  OR  ",
                    style = AppTheme.typography.labelSmall.copy(
                        color = AppTheme.colors.muted,
                        fontWeight = FontWeight.Bold
                    )
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = AppTheme.colors.border
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Continue as Guest Button
            SecondaryButton(
                text = "Continue as Guest",
                onClick = { onEvents(LoginScreenUiEvents.ContinueAsGuest) },
                icon = Icons.Default.Person,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer: Don't have an account? Sign Up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.muted
                    )
                )
                Text(
                    text = "Sign Up",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { createNewAccount() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
