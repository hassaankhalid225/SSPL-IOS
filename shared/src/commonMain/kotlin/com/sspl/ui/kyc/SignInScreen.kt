package com.sspl.ui.kyc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.sharp.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.eygraber.compose.country.code.picker.CountryCodePicker
import com.sspl.Screen
import com.sspl.resources.Res
import com.sspl.resources.back
import com.sspl.resources.create_new_account
import com.sspl.resources.enter_your_mail
import com.sspl.resources.enter_your_password
import com.sspl.resources.forgot_password
import com.sspl.resources.get_updates
import com.sspl.resources.login
import com.sspl.resources.sign_in_via_email
import com.sspl.resources.sign_in_via_phone
import com.sspl.resources.sign_up
import com.sspl.resources.welcome
import com.sspl.theme.boxColor
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppText
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextField
import com.sspl.ui.components.AppTextSmall
import com.sspl.ui.components.AppTextTitle
import com.sspl.ui.components.PasswordTextField
import com.sspl.ui.components.PhoneWithCountrySelector
import com.vanniktech.locale.Country
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 09/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.oneTimeEvents.collect { event ->
            when (event) {
                OneTimeEvents.LoginSuccess -> {

                }

                OneTimeEvents.NavigateToOtpScreen -> {
                    navController.navigate(Screen.OtpScreen.route)
                }

                is OneTimeEvents.ShowError -> {
                    scaffoldState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(scaffoldState) }) { padding ->

        LoginScreenContent(
            email = uiState.email,
            password = uiState.password,
            phone = uiState.phoneNo, selectedCountry = uiState.country,
            isOtpMode = uiState.isOtpMode,
            showPassword = uiState.isPasswordVisible,
            emailErrorMsg = uiState.emailError,
            passwordErrorMsg = uiState.passwordError,
            phoneErrorMsg = uiState.phoneNoError,
            isLoading = uiState.isLoading, showCountryChooser = uiState.showCountryChooser,
            onEvents = viewModel::onEvent,
            createNewAccount = {
                navController.navigate(Screen.SignUpScreen.route)
            },
            modifier = modifier
        )
        }


}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    onEvents: (LoginScreenUiEvents) -> Unit = {},
    emailErrorMsg:String?,
    passwordErrorMsg:String?,
    phoneErrorMsg:String?,
    isLoading: Boolean = true, selectedCountry: Country,
    showCountryChooser: Boolean = false,
    email: String = "", phone: String = "",
    password: String = "",createNewAccount:()->Unit,
    showPassword: Boolean = false,
    onForgotPassword: () -> Unit = {},
    isOtpMode: Boolean = false
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp), verticalArrangement = Arrangement.Top
    ) {
        AppTextTitle(
            text = stringResource(Res.string.welcome),
            fontSize = 14.sp,
        )
        AppTextTitle(
            text = stringResource(Res.string.back),
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(10.dp))
        AppTextSmall(stringResource(Res.string.get_updates), color = Color.LightGray)
        Spacer(Modifier.weight(0.15f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = modifier.weight(1f)
        ) {
            AnimatedVisibility(!isOtpMode) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppTextField(
                        placeholder = stringResource(Res.string.enter_your_mail),
                        modifier = Modifier.fillMaxWidth(),
                        text = email,
                        errorMessage = emailErrorMsg,
                        onTextChange = { onEvents.invoke(LoginScreenUiEvents.OnEmailTextChange(it)) })

                    PasswordTextField(
                        placeholder = stringResource(Res.string.enter_your_password),
                        modifier = Modifier.fillMaxWidth(),
                        text = password, errorMessage = passwordErrorMsg,
                        onTogglePassword = {
                            onEvents.invoke(LoginScreenUiEvents.TogglePasswordVisibility)
                        },
                        isPasswordVisible = showPassword,
                        onTextChange = { onEvents.invoke(LoginScreenUiEvents.OnPasswordTextChange(it)) })
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        AppTextSmall(
                            stringResource(Res.string.forgot_password),
                            color = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.clickable {
                                onForgotPassword()
                            })
                    }
                }
            }
            AnimatedVisibility(isOtpMode) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PhoneWithCountrySelector(
                        selectedCountry = selectedCountry,
                        phone = phone,
                        onPhoneNumberChange = {
                            onEvents.invoke(LoginScreenUiEvents.OnPhoneNoChange(it))
                        },
                        showCountryChooser = showCountryChooser,
                        onCountryChange = {
                            onEvents.invoke(LoginScreenUiEvents.OnCountryChange(it))
                        },
                        toggleCountryPicker = {
                            onEvents.invoke(LoginScreenUiEvents.ToggleCountryChooser)}

                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            AppRoundedCornerButton(
                onClick = {
                    onEvents.invoke(LoginScreenUiEvents.OnSave)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppTextBody(
                        stringResource(Res.string.login),
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    androidx.compose.animation.AnimatedVisibility(isLoading) {
                        Row() {
                            Spacer(Modifier.weight(1f))
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
            // Add "Sign in with Phone" Button
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
                Text("or", fontSize = 8.sp)

                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
            }
            AppRoundedCornerButton(
                onClick = {
                    onEvents.invoke(LoginScreenUiEvents.ToggleOtpMode) // Trigger phone sign in event
                }, title =if (isOtpMode) stringResource(Res.string.sign_in_via_email) else stringResource(
                    Res.string.sign_in_via_phone
                ) ,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
            AppRoundedCornerButton(
                onClick = createNewAccount, title = stringResource( Res.string.create_new_account)
                 ,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
        }

    }
}




