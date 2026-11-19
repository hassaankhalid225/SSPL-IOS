package com.sspl.ui.kyc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A5: Modernized Forgot & Reset Password flow with 2-step wizard.
 */
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val scaffoldState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Status for sending reset code
    LaunchedEffect(Unit) {
        viewModel.initPasswordStatus.collect { event ->
            when (event) {
                is ApiStates.Failure -> {
                    scaffoldState.showSnackbar(event.error.message)
                }
                else -> Unit
            }
        }
    }

    // Status for resetting password
    LaunchedEffect(Unit) {
        viewModel.updatePasswordStatus.collect { event ->
            when (event) {
                is ApiStates.Failure -> {
                    scaffoldState.showSnackbar(event.error.message)
                }
                is ApiStates.Success -> {
                    scaffoldState.showSnackbar("Password reset successfully. Please sign in.")
                    navController.navigate(Screen.SignInScreen.route) {
                        popUpTo(Screen.ForgotPasswordScreen.route) { inclusive = true }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Back Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (uiState.showOtpView) {
                                viewModel.switchView(false)
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.onPrimary
                        )
                    }
                    Text(
                        text = if (uiState.showOtpView) "Enter Reset Code" else "Reset Password",
                        style = AppTheme.typography.titleMedium.copy(
                            color = AppTheme.colors.heading,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SSPL Logo
                Image(
                    painter = painterResource(Res.drawable.app_logo),
                    contentDescription = "SSPL Logo",
                    modifier = Modifier.size(70.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (uiState.showOtpView) "Set New Password" else "Forgot Your Password?",
                    style = AppTheme.typography.headlineMedium.copy(
                        color = AppTheme.colors.heading,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (uiState.showOtpView)
                        "Enter the 6-digit verification code sent to your email and your new password"
                    else
                        "Enter your registered email address and we'll send you a password reset code",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.muted
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Step Indicator
                StepIndicator(
                    currentStep = if (uiState.showOtpView) 2 else 1,
                    totalSteps = 2,
                    stepLabels = listOf("Verify Email", "New Password")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Step 1: Email Form
                AnimatedVisibility(visible = !uiState.showOtpView) {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SsplTextField(
                                value = uiState.emailText,
                                onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.EmailChanged(it)) },
                                label = "Registered Email Address",
                                placeholder = "doctor@example.com",
                                leadingIcon = Icons.Default.Email,
                                errorMessage = uiState.emailError,
                                isError = uiState.emailError != null
                            )

                            PrimaryButton(
                                text = "Send Reset Code",
                                onClick = { viewModel.onEvent(ForgotPasswordUiEvents.InitPassword) },
                                isLoading = uiState.isLoadingInitPassword,
                                enabled = uiState.isEnabledInitPassword && !uiState.isLoadingInitPassword,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Step 2: Code + Password Form
                AnimatedVisibility(visible = uiState.showOtpView) {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SsplTextField(
                                value = uiState.otpString,
                                onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.OnOtpEntered(it)) },
                                label = "Verification Code",
                                placeholder = "Enter 6-digit code",
                                leadingIcon = Icons.Default.Key
                            )

                            PasswordField(
                                value = uiState.newPassword,
                                onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.NewPasswordChanged(it)) },
                                label = "New Password",
                                placeholder = "At least 8 characters",
                                isVisible = uiState.isPasswordVisible,
                                onToggleVisibility = { viewModel.onEvent(ForgotPasswordUiEvents.TogglePasswordVisibility) },
                                errorMessage = uiState.newPasswordError,
                                isError = uiState.newPasswordError != null
                            )

                            PasswordField(
                                value = uiState.confirmPassword,
                                onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.ConfirmPasswordChanged(it)) },
                                label = "Confirm New Password",
                                placeholder = "Re-enter new password",
                                isVisible = uiState.isReenteredPasswordVisible,
                                onToggleVisibility = { viewModel.onEvent(ForgotPasswordUiEvents.ToggleReenteredPasswordVisibility) },
                                errorMessage = uiState.confirmPasswordError,
                                isError = uiState.confirmPasswordError != null
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            PrimaryButton(
                                text = "Reset Password",
                                onClick = { viewModel.onEvent(ForgotPasswordUiEvents.UpdatePassword) },
                                isLoading = uiState.isLoadingUpdatePassword,
                                enabled = uiState.isEnabledUpdatePassword && !uiState.isLoadingUpdatePassword,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButtonLink(
                                    text = "Didn't receive the code? Resend",
                                    onClick = { viewModel.onEvent(ForgotPasswordUiEvents.InitPassword) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Back to Login link
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButtonLink(
                        text = "Back to Sign In",
                        onClick = { navController.navigate(Screen.SignInScreen.route) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
