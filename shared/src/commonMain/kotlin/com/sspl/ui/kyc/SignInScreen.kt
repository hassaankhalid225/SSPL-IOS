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
                OneTimeEvents.LoginSuccess -> {}
                OneTimeEvents.NavigateToOtpScreen -> navController.navigate(Screen.OtpScreen.route)
                is OneTimeEvents.ShowError -> scaffoldState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) }
    ) { _ ->
        LoginScreenContent(
            uiState = uiState,
            onEvents = viewModel::onEvent,
            createNewAccount = { navController.navigate(Screen.SignUpScreen.route) },
            modifier = modifier
        )
    }
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEvents: (LoginScreenUiEvents) -> Unit,
    createNewAccount: () -> Unit,
    modifier: Modifier = Modifier,
    onForgotPassword: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        LoginHeader()
        Spacer(Modifier.weight(0.15f))
        LoginForm(
            uiState = uiState,
            onEvents = onEvents,
            onForgotPassword = onForgotPassword,
            createNewAccount = createNewAccount,
            modifier = Modifier.weight(1f)
        )

    }
}

@Composable
private fun LoginHeader() {
    AppTextTitle(text = stringResource(Res.string.welcome), fontSize = 14.sp)
    AppTextTitle(text = stringResource(Res.string.back), fontSize = 14.sp)
    Spacer(Modifier.height(10.dp))
    AppTextSmall(stringResource(Res.string.get_updates), color = Color.LightGray)
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    onEvents: (LoginScreenUiEvents) -> Unit,
    onForgotPassword: () -> Unit,
    createNewAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        if (!uiState.isOtpMode) {
            EmailPasswordForm(uiState, onEvents, onForgotPassword)
        } else {
            PhoneForm(uiState, onEvents)
        }
        LoginButtons(uiState, onEvents, createNewAccount)
    }
}

@Composable
private fun EmailPasswordForm(
    uiState: LoginUiState,
    onEvents: (LoginScreenUiEvents) -> Unit,
    onForgotPassword: () -> Unit
) {
    LoginTextField(
        placeholder = stringResource(Res.string.enter_your_mail),
        text = uiState.email,
        onTextChange = { onEvents(LoginScreenUiEvents.OnEmailTextChange(it)) },
        modifier = Modifier.fillMaxWidth()
    )
    LoginTextField(
        placeholder = stringResource(Res.string.enter_your_password),
        text = uiState.password,
        onTextChange = { onEvents(LoginScreenUiEvents.OnPasswordTextChange(it)) },
        onTogglePassword = { onEvents(LoginScreenUiEvents.TogglePasswordVisibility) },
        isPassword = true,
        isPasswordVisible = uiState.isPasswordVisible,
        modifier = Modifier.fillMaxWidth()
    )
    Row(Modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(1f))
        AppTextSmall(
            stringResource(Res.string.forgot_password),
            color = Color.Red.copy(alpha = 0.7f),
            modifier = Modifier.clickable(onClick = onForgotPassword)
        )
    }
}

@Composable
private fun PhoneForm(
    uiState: LoginUiState,
    onEvents: (LoginScreenUiEvents) -> Unit
) {
    PhoneWithCountrySelector(
        selectedCountry = uiState.country,
        phone = uiState.phoneNo,
        onPhoneNumberChange = { onEvents(LoginScreenUiEvents.OnPhoneNoChange(it)) },
        showCountryChooser = uiState.showCountryChooser,
        onCountryChange = { onEvents(LoginScreenUiEvents.OnCountryChange(it)) },
        toggleCountryPicker = { onEvents(LoginScreenUiEvents.ToggleCountryChooser) }
    )
}

@Composable
private fun LoginButtons(
    uiState: LoginUiState,
    onEvents: (LoginScreenUiEvents) -> Unit,
    createNewAccount: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    LoginButton(uiState.isLoading) { onEvents(LoginScreenUiEvents.OnSave) }
    Spacer(modifier = Modifier.height(16.dp))
    OrDivider()
    ToggleLoginModeButton(uiState.isOtpMode) { onEvents(LoginScreenUiEvents.ToggleOtpMode) }
    CreateAccountButton(createNewAccount)
}

@Composable
fun ToggleLoginModeButton(isOtpMode:Boolean,toggleOtpMode:()->Unit) {
    AppRoundedCornerButton(
        onClick = toggleOtpMode
        , title =if (isOtpMode) stringResource(Res.string.sign_in_via_email) else stringResource(
            Res.string.sign_in_via_phone
        ) ,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    )
}
@Composable
fun CreateAccountButton(createNewAccount: () -> Unit) {
    AppRoundedCornerButton(
        onClick = createNewAccount, title = stringResource( Res.string.create_new_account)
        ,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    )
}
@Composable
private fun LoginButton(isLoading: Boolean, onClick: () -> Unit) {
    AppRoundedCornerButton(
        onClick = onClick,
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
            AnimatedVisibility(isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun OrDivider() {
    Row(horizontalArrangement = Arrangement.Center) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text("or", fontSize = 8.sp)
        HorizontalDivider(modifier = Modifier.weight(1f))
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
fun PhoneWithCountrySelector(modifier: Modifier = Modifier, selectedCountry: Country, phone: String, onPhoneNumberChange: (String) -> kotlin.Unit, showCountryChooser: Boolean, onCountryChange:(Country)->Unit, toggleCountryPicker: () -> Unit) {
    Row(modifier) {
        androidx.compose.animation.AnimatedVisibility(showCountryChooser) {
            CountryCodePicker(
                initialCountry = Country.PAKISTAN,
                onClick = { country ->
                    onCountryChange.invoke(country)
                },
            )
        }
        PhoneNumberTextField(
            selectedCountry = selectedCountry,
            phoneNumber = phone,
            onPhoneNumberChange = {
                onPhoneNumberChange.invoke(it)
            },
            toggleCountryPicker = {
               toggleCountryPicker.invoke()
                  })
    }
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

