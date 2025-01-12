package com.sspl.ui.otp

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.resources.Res
import com.sspl.resources.enter_otp_sent_to_your_number
import com.sspl.resources.please_verify_your_phone_number
import com.sspl.resources.resend_it_now
import com.sspl.resources.still_waiting_for_your_otp
import com.sspl.resources.verification_code
import com.sspl.resources.verify_otp
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextSmall
import com.sspl.ui.components.AppTextSubTitle
import com.sspl.ui.components.AppTextTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun OtpScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = koinViewModel()
) {
    val scaffoldState = remember { SnackbarHostState() }
    val isVerifying by viewModel.isVerifying.collectAsState()
    val otpValue by viewModel.otpValue.collectAsState()
    LaunchedEffect(Unit){
        viewModel.resultFlow.collect{ result->
            when(result){
                is OtpOneTimeEvents.ShowMessage -> {
                    scaffoldState.showSnackbar(result.message)
                }
                OtpOneTimeEvents.Success -> {
                    navController.navigate(Screen.HomeScreen.route)
                }
            }

        }
    }

    Scaffold(snackbarHost = { SnackbarHost(scaffoldState) }) { _ ->
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isVerifying) 0.3f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OtpView(
                    otpLength = 6,
                    otpValue = otpValue,
                    onOtpChange = viewModel::onOTPValueChange,
                    onOtpEntered = viewModel::verifyOtp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppTextSmall(text = stringResource(Res.string.still_waiting_for_your_otp))
                    AppTextSmall(
                        text = stringResource(Res.string.resend_it_now),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = !isVerifying) {
                            // Handle resend action
                            viewModel.resendOtp()
                        }
                    )
                }
            }

            if (isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }


}

@Composable
fun OtpView(
    otpLength: Int = 6,
    otpValue: String,
    onOtpChange:(String)->Unit,
    onOtpEntered: (String) -> Unit
) {
 val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(100) // Short delay to ensure the view is ready
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppTextTitle(
            text = stringResource(Res.string.verification_code),
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppTextSubTitle(
            text = stringResource(Res.string.enter_otp_sent_to_your_number),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(otpLength) { index ->
                OtpDigit(
                    value = otpValue.getOrNull(index)?.toString() ?: "",
                    onClick = {
                        scope.launch {
                            focusManager.clearFocus(force = true)
                            delay(100) // Short delay to ensure focus is cleared
                            focusRequester.requestFocus()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = otpValue,
            onValueChange = { newValue ->
                if (newValue.length <= otpLength) {
                    onOtpChange(newValue.filter { it.isDigit() })
                    if (newValue.length == otpLength) {
                        onOtpEntered(otpValue)
                        focusManager.clearFocus() // Dismiss keyboard when all digits are entered
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .focusRequester(focusRequester)
        )
    }
}

@Composable
fun OtpDigit(value: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppTextSubTitle(
            text = value,
        )
    }
}
