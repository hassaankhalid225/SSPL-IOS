package com.sspl.ui.otp

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.kyc.AuthScaffold
import com.sspl.ui.kyc.SheetHeading
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * Phone verification (6-digit code). Reached only from the phone sign-in mode, which is currently
 * hidden on the login screen (no phone-OTP endpoint on the backend; see OtpViewModel).
 */
@Composable
fun OtpScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.resultFlow.collect { result ->
            when (result) {
                is OtpOneTimeEvents.ShowMessage -> snackbar.showSnackbar(result.message)
                OtpOneTimeEvents.Success -> navController.navigate(Screen.MainScreen.route) {
                    popUpTo(Screen.SignInScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    AuthScaffold(
        title = "Verify your number",
        subtitle = "Enter the 6-digit code we sent to your mobile",
        snackbarHostState = snackbar,
        onBack = { navController.popBackStack() },
        modifier = modifier
    ) {
        SheetHeading(title = "Verification code", caption = "The code expires in a few minutes.")
        Spacer(Modifier.height(22.dp))

        OtpCodeInput(
            value = uiState.otpValue,
            onValueChange = { viewModel.onEvent(OtpEvent.OtpValueChanged(it)) },
            onComplete = { viewModel.onEvent(OtpEvent.VerifyOtp(it)) },
            enabled = !uiState.isVerifying
        )

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Verify and continue",
            onClick = { viewModel.onEvent(OtpEvent.VerifyOtp(uiState.otpValue)) },
            loading = uiState.isVerifying,
            enabled = uiState.otpValue.length == 6
        )
        Spacer(Modifier.height(12.dp))
        ResendRow(
            secondsLeft = uiState.resendSecondsLeft,
            onResend = { viewModel.onEvent(OtpEvent.ResendOtp) }
        )
    }

    if (uiState.isVerifying) BlockingLoader("Verifying code…")
}

/** "Didn't get a code? Resend" with a countdown. */
@Composable
fun ResendRow(secondsLeft: Int, onResend: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Didn't get a code? ", style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted))
        if (secondsLeft > 0) {
            val mm = secondsLeft / 60
            val ss = (secondsLeft % 60).toString().padStart(2, '0')
            Text(
                text = "Resend in $mm:$ss",
                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted, fontWeight = FontWeight.SemiBold)
            )
        } else {
            TextButtonLink(text = "Resend code", onClick = onResend, enabled = enabled)
        }
    }
}

/**
 * Six-box one-time-code input. A single hidden-caret text field drives all boxes, so paste,
 * backspace and SMS autofill all work. Requests focus on first composition.
 */
@Composable
fun OtpCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
    autoFocus: Boolean = true,
    onComplete: ((String) -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            delay(200)
            runCatching { focusRequester.requestFocus() }
        }
    }

    BasicTextField(
        value = TextFieldValue(value, selection = TextRange(value.length)),
        onValueChange = { tfv ->
            val digits = tfv.text.filter { it.isDigit() }.take(length)
            if (digits != value) {
                onValueChange(digits)
                if (digits.length == length) {
                    focusManager.clearFocus()
                    onComplete?.invoke(digits)
                }
            }
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            if (value.length == length) onComplete?.invoke(value)
        }),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = "$length digit verification code" },
        decorationBox = { _ ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(length) { index ->
                    OtpBox(
                        char = value.getOrNull(index)?.toString().orEmpty(),
                        active = enabled && index == value.length.coerceAtMost(length - 1) && value.length < length,
                        isError = isError,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpBox(char: String, active: Boolean, isError: Boolean, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val borderWidth by animateDpAsState(if (active) 2.dp else 1.dp, label = "otpBorder")
    val borderColor = when {
        isError -> colors.error
        active -> colors.primary
        char.isNotEmpty() -> colors.primary.copy(alpha = 0.45f)
        else -> colors.border
    }
    Box(
        modifier = modifier
            .widthIn(max = 52.dp)
            .aspectRatio(0.86f)
            .clip(AppTheme.shapes.small)
            .background(if (char.isNotEmpty()) colors.surfaceTint else colors.boxColor)
            .border(borderWidth, borderColor, AppTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        if (char.isEmpty() && active) {
            Box(Modifier.height(22.dp).width(2.dp).background(colors.primary))
        } else {
            Text(
                text = char,
                style = AppTheme.typography.titleLarge.copy(color = colors.heading, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }
}
