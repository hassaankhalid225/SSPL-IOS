package com.sspl.ui.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * A4: Modernized OTP Verification Screen with Design System styling.
 */
@Composable
fun OtpScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.resultFlow.collect { result ->
            when (result) {
                is OtpOneTimeEvents.ShowMessage -> {
                    scope.launch {
                        scaffoldState.showSnackbar(result.message)
                    }
                }
                OtpOneTimeEvents.Success -> {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.SignInScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        containerColor = AppTheme.colors.background
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with back navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.onPrimary
                        )
                    }
                    Text(
                        text = "Phone Verification",
                        style = AppTheme.typography.titleMedium.copy(
                            color = AppTheme.colors.heading,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Verification Code",
                    style = AppTheme.typography.headlineMedium.copy(
                        color = AppTheme.colors.heading,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please enter the 6-digit code sent to your mobile number",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = AppTheme.colors.muted
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OtpView(
                            otpLength = 6,
                            otpValue = uiState.otpValue,
                            onOtpChange = { viewModel.onEvent(OtpEvent.OtpValueChanged(it)) },
                            onOtpEntered = { viewModel.onEvent(OtpEvent.VerifyOtp(it)) }
                        )

                        PrimaryButton(
                            text = "Verify & Continue",
                            onClick = { viewModel.onEvent(OtpEvent.VerifyOtp(uiState.otpValue)) },
                            isLoading = uiState.isVerifying,
                            enabled = uiState.otpValue.length == 6 && !uiState.isVerifying,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Resend OTP row
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Didn't receive code? ",
                                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                            )
                            TextButtonLink(
                                text = "Resend Code",
                                onClick = { viewModel.onEvent(OtpEvent.ResendOtp) }
                            )
                        }
                    }
                }
            }

            if (uiState.isVerifying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.secondary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

@Composable
fun OtpView(
    otpLength: Int = 6,
    otpValue: String,
    onOtpChange: (String) -> Unit,
    onOtpEntered: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(otpLength) { index ->
                val char = otpValue.getOrNull(index)?.toString() ?: ""
                val isFocused = otpValue.length == index
                OtpDigit(
                    value = char,
                    isFocused = isFocused,
                    onClick = {
                        scope.launch {
                            focusManager.clearFocus(force = true)
                            delay(100)
                            focusRequester.requestFocus()
                        }
                    }
                )
            }
        }

        BasicTextField(
            value = otpValue,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }
                if (filtered.length <= otpLength) {
                    onOtpChange(filtered)
                    if (filtered.length == otpLength) {
                        onOtpEntered(filtered)
                        focusManager.clearFocus()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .size(0.dp)
                .focusRequester(focusRequester)
        )
    }
}

@Composable
fun OtpDigit(
    value: String,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isFocused) {
        AppTheme.colors.secondary
    } else if (value.isNotEmpty()) {
        AppTheme.colors.primaryLight
    } else {
        AppTheme.colors.border
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.surface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = AppTheme.typography.titleLarge.copy(
                color = AppTheme.colors.heading,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}
