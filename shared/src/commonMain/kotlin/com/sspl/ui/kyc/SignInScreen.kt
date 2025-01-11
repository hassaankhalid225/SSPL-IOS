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
import com.sspl.resources.enter_your_mail
import com.sspl.resources.enter_your_password
import com.sspl.resources.forgot_password
import com.sspl.resources.get_updates
import com.sspl.resources.login
import com.sspl.resources.sign_in_via_email
import com.sspl.resources.sign_in_via_phone
import com.sspl.resources.welcome
import com.sspl.theme.boxColor
import com.sspl.ui.components.AppText
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextSmall
import com.sspl.ui.components.AppTextTitle
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

        LoginScreenContent(
            email = uiState.email,
            password = uiState.password,
            phone = uiState.phoneNo, selectedCountry = uiState.country,
            isOtpMode = uiState.isOtpMode,
            showPassword = uiState.isPasswordVisible,
            isLoading = uiState.isLoading, showCountryChooser = uiState.showCountryChooser,
            onEvents = viewModel::onEvent,
            modifier = modifier,
        )


}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    onEvents: (LoginScreenUiEvents) -> Unit = {},
    isLoading: Boolean = true, selectedCountry: Country,
    showCountryChooser: Boolean = false,
    email: String = "", phone: String = "",
    password: String = "",
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
        Spacer(Modifier.weight(0.2f))
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


                    LoginTextField(
                        placeholder = stringResource(Res.string.enter_your_mail),
                        modifier = Modifier.fillMaxWidth(),
                        text = email,
                        onTextChange = { onEvents.invoke(LoginScreenUiEvents.OnEmailTextChange(it)) })

                    LoginTextField(
                        placeholder = stringResource(Res.string.enter_your_password),
                        modifier = Modifier.fillMaxWidth(),
                        text = password,
                        onTogglePassword = {
                            onEvents.invoke(LoginScreenUiEvents.TogglePasswordVisibility)
                        },
                        isPassword = true,
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
                    Row {
                        androidx.compose.animation.AnimatedVisibility(showCountryChooser) {
                            CountryCodePicker(
                                initialCountry = Country.PAKISTAN,
                                onClick = { country ->
                                    onEvents.invoke(LoginScreenUiEvents.OnCountryChange(country))
                                },
                            )
                        }
                        PhoneNumberTextField(
                            selectedCountry = selectedCountry,
                            phoneNumber = phone,
                            onPhoneNumberChange = {
                                onEvents.invoke(LoginScreenUiEvents.OnPhoneNoChange(it))
                            },
                            toggleCountryPicker = {
                                onEvents.invoke(LoginScreenUiEvents.ToggleCountryChooser)
                            })
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            Button(
                onClick = {
                    onEvents.invoke(LoginScreenUiEvents.OnSave)
                },
                shape = RoundedCornerShape(16),
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
            Button(
                onClick = {
                    onEvents.invoke(LoginScreenUiEvents.ToggleOtpMode) // Trigger phone sign in event
                },
                shape = RoundedCornerShape(16),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                AppTextBody(
                    if (isOtpMode) stringResource(Res.string.sign_in_via_email) else stringResource(
                        Res.string.sign_in_via_phone
                    ),
                    fontSize = 12.sp, color = Color.White
                )
            }
        }
        Spacer(Modifier.weight(0.85f))

    }
}

@Composable
fun LoginTextField(
    modifier: Modifier = Modifier,
    text: String?, isPassword: Boolean = false,
    isPasswordVisible: Boolean = false, onTogglePassword: () -> Unit = {},
    placeholder: String = "Enter your text here...",
    shape: Shape = RoundedCornerShape(22), keyboardType: KeyboardType = KeyboardType.Text,

    onTextChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    TextField(
        value = text ?: "",
        onValueChange = { onTextChange.invoke(it) },
        visualTransformation = when {
            isPassword && !isPasswordVisible -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        placeholder = {
            AppTextBody(
                placeholder, fontSize = 9.sp, fontFamily = FontFamily.Serif
            )
        },
        modifier = modifier
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .border(
                width = 1.dp,
                color = if (!isFocused) boxColor else Color.LightGray,
                shape = shape
            ),
        maxLines = 6,
        singleLine = false,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done, keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(22),
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onTogglePassword() }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Favorite else Icons.Sharp.Call,
                        contentDescription = "Hide password"
                    )
                }
            }
        },

        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black.copy(0.7f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }),
    )
}

@Composable
fun PhoneNumberTextField(
    modifier: Modifier = Modifier,
    selectedCountry: Country = Country.PAKISTAN,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    toggleCountryPicker: () -> Unit,
    placeholder: String = "Mobile Number",
    shape: Shape = RoundedCornerShape(22),
    keyboardType: KeyboardType = KeyboardType.Phone
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    TextField(
        value = phoneNumber,
        onValueChange = { onPhoneNumberChange(it) },
        placeholder = {
            Text(
                placeholder,
                fontSize = 9.sp,
                fontFamily = FontFamily.Serif
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .border(
                width = 1.dp,
                color = if (!isFocused) boxColor else Color.LightGray,
                shape = shape
            ),
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        shape = shape,
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = toggleCountryPicker)
            ) {
                AsyncImage(
                    model = selectedCountry.emoji,
                    contentDescription = "Country flag",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedCountry.code,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select country code"
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black.copy(0.7f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }),
        visualTransformation = PhonePrefixTransformation(
            selectedCountry.callingCodes.first().toString()
        )
    )
}

// Assuming you have this class defined
class PhonePrefixTransformation(private val prefix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val prefixOffset = prefix.length + 1 // +1 for the space
        val annotatedString = buildAnnotatedString {
            append(prefix)
            append(" ")
            append(text)
        }
        return TransformedText(annotatedString, PhonePrefixOffsetMapping(prefixOffset))
    }

    private class PhonePrefixOffsetMapping(private val prefixOffset: Int) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return offset + prefixOffset
        }

        override fun transformedToOriginal(offset: Int): Int {
            return (offset - prefixOffset).coerceAtLeast(0)
        }
    }
}

