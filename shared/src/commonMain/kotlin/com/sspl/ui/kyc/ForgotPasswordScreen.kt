package com.sspl.ui.kyc

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.LockReset
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
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.otp.OtpCodeInput
import com.sspl.ui.otp.ResendRow
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Forgot / reset password (2 steps: email -> code + new password).
 */
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val snackbar = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val goToSignIn: () -> Unit = {
        if (!navController.popBackStack(Screen.SignInScreen.route, inclusive = false)) {
            navController.navigate(Screen.SignInScreen.route) {
                popUpTo(Screen.ForgotPasswordScreen.route) { inclusive = true }
            }
        }
    }
    val onBack: () -> Unit = {
        if (uiState.showOtpView) viewModel.switchView(false) else navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        viewModel.initPasswordStatus.collect { event ->
            if (event is ApiStates.Failure) {
                snackbar.showSnackbar(event.error.message.ifBlank { "Could not send reset code. Check your email and try again." })
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updatePasswordStatus.collect { event ->
            if (event is ApiStates.Success) {
                if (viewModel.uiState.value.signedInDirectly) {
                    // Reset returned an access token: go straight into the app (web parity).
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    snackbar.showSnackbar("Password updated. You can sign in with your new password.")
                    delay(600)
                    goToSignIn()
                }
            }
        }
    }

    AuthScaffold(
        title = if (uiState.showOtpView) "Set a new password" else "Forgot password?",
        subtitle = if (uiState.showOtpView) "Enter the code from your email and choose a new password"
        else "We'll email you a 6-digit code to reset it",
        snackbarHostState = snackbar,
        onBack = onBack,
        heroFraction = 0.34f
    ) {
        StepIndicator(
            currentStep = if (uiState.showOtpView) 2 else 1,
            totalSteps = 2,
            stepLabels = listOf("Verify email", "New password")
        )
        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(visible = uiState.formError != null) {
            FeedbackBanner(uiState.formError.orEmpty(), isError = true, modifier = Modifier.padding(bottom = 14.dp))
        }

        AnimatedContent(
            targetState = uiState.showOtpView,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
            label = "resetStep"
        ) { showOtp ->
            if (!showOtp) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SsplTextField(
                        value = uiState.emailText,
                        onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.EmailChanged(it)) },
                        label = "Registered email address",
                        placeholder = "doctor@example.com",
                        leadingIcon = SsplIcons.AlternateEmail,
                        errorMessage = uiState.emailError,
                        keyboardType = KeyboardType.Email,
                        enabled = !uiState.isLoadingInitPassword,
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (uiState.isEnabledInitPassword) viewModel.onEvent(ForgotPasswordUiEvents.InitPassword)
                        })
                    )
                    PrimaryButton(
                        text = "Send reset code",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onEvent(ForgotPasswordUiEvents.InitPassword)
                        },
                        loading = uiState.isLoadingInitPassword,
                        enabled = uiState.isEnabledInitPassword,
                        trailingIcon = SsplIcons.Send
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    uiState.infoMessage?.let { FeedbackBanner(it, isError = false) }
                    Text("Verification code", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading))
                    OtpCodeInput(
                        value = uiState.otpString,
                        onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.OnOtpEntered(it)) },
                        onComplete = { focusManager.moveFocus(FocusDirection.Down) },
                        enabled = !uiState.isLoadingUpdatePassword
                    )
                    ResendRow(
                        secondsLeft = uiState.resendSecondsLeft,
                        onResend = { viewModel.onEvent(ForgotPasswordUiEvents.InitPassword) },
                        enabled = !uiState.isLoadingInitPassword
                    )
                    PasswordField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.NewPasswordChanged(it)) },
                        label = "New password",
                        placeholder = "At least 8 characters",
                        isVisible = uiState.isPasswordVisible,
                        onToggleVisibility = { viewModel.onEvent(ForgotPasswordUiEvents.TogglePasswordVisibility) },
                        errorMessage = uiState.newPasswordError,
                        enabled = !uiState.isLoadingUpdatePassword,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    PasswordField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onEvent(ForgotPasswordUiEvents.ConfirmPasswordChanged(it)) },
                        label = "Confirm new password",
                        placeholder = "Re-enter new password",
                        isVisible = uiState.isReenteredPasswordVisible,
                        onToggleVisibility = { viewModel.onEvent(ForgotPasswordUiEvents.ToggleReenteredPasswordVisibility) },
                        errorMessage = uiState.confirmPasswordError,
                        enabled = !uiState.isLoadingUpdatePassword,
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.onEvent(ForgotPasswordUiEvents.UpdatePassword)
                        })
                    )
                    Spacer(Modifier.height(4.dp))
                    PrimaryButton(
                        text = "Reset password",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onEvent(ForgotPasswordUiEvents.UpdatePassword)
                        },
                        loading = uiState.isLoadingUpdatePassword,
                        enabled = uiState.isEnabledUpdatePassword,
                        icon = SsplIcons.LockReset
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            TextButtonLink(text = "Back to sign in", onClick = goToSignIn)
        }
    }
}
