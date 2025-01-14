package com.sspl.ui.kyc


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.resources.Res
import com.sspl.resources.already_have_an_account
import com.sspl.resources.create_an_account_so_you_can_interact_seamlessly
import com.sspl.resources.create_new_account
import com.sspl.resources.enter_first_name
import com.sspl.resources.enter_last_name
import com.sspl.resources.enter_your_mail
import com.sspl.resources.enter_your_password
import com.sspl.resources.login
import com.sspl.resources.login_screen_background
import com.sspl.theme.primary
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppTextField
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextTitle
import com.sspl.ui.components.PasswordTextField
import com.sspl.ui.components.PhoneWithCountrySelector
import com.vanniktech.locale.Country
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


@Preview
@Composable
fun SignUpScreen(
    navController: NavController, viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()


    SignUpScreenContent(
        modifier = Modifier,
        email = uiState.email,
        isSignUpEnabled = uiState.isSignUpEnabled,
        firstName = uiState.firstName,
        lastName = uiState.lastName,
        reEnterPassword = uiState.retypedPassword,
        reEnterPasswordErrorMsg = uiState.retypedPasswordError,
        isReEnteredVisible = uiState.isReenteredPasswordVisible,
        isLoading = uiState.isLoading,
        passwordErrorMsg = uiState.passwordError,
        password = uiState.password,
        isPasswordVisible = uiState.isPasswordVisible,
        showCountryChooser = uiState.showCountryChooser,
        country = uiState.country,
        emailErrorMsg = uiState.emailError, firstNameErrorMsg = uiState.firstNameError,
        lastNameErrorMsg = uiState.lastNameError,
        phoneNo = uiState.phoneNo,
        loginScreen = {
            navController.navigate(Screen.SignInScreen.route)
        },
        onEvent = viewModel::onEvent,
    )


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
    password: String,
    isPasswordVisible: Boolean,
    showCountryChooser: Boolean,
    country: Country,
    emailErrorMsg: String?,
    phoneNo: String,
    loginScreen: () -> Unit,
    onEvent: (UserEvents) -> Unit
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_screen_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.15f))
            HeaderSection()
            Spacer(Modifier.weight(0.15f))
            // Input Fields Section
            InputFieldsSection(
                firstName = firstName,
                lastName = lastName,
                reEnterPassword = reEnterPassword,
                reEnterPasswordErrorMsg = reEnterPasswordErrorMsg,
                isReEnteredVisible = isReEnteredVisible,
                passwordErrorMsg = passwordErrorMsg,
                firstNameErrorMsg = firstNameErrorMsg,
                lastNameErrorMsg = lastNameErrorMsg,
                password = password,
                isPasswordVisible = isPasswordVisible,
                country = country,
                email = email,
                emailErrorMsg = emailErrorMsg,
                phoneNo = phoneNo,
                showCountryChooser = showCountryChooser,
                onEvent = onEvent
            )

            Spacer(modifier = Modifier.height(10.dp))

            SignUpButtonSection(
                isSignUpEnabled = isSignUpEnabled, isLoading = isLoading, onEvent = onEvent
            )

            AlreadyHaveAccountSection(loginScreen)

            Spacer(Modifier.weight(0.7f))
        }
    }
}

@Composable
fun HeaderSection() {

    AppTextTitle(
        text = stringResource(Res.string.create_new_account), fontSize = 26.sp, color = primary
    )
    AppTextTitle(
        text = stringResource(Res.string.create_an_account_so_you_can_interact_seamlessly),
        textAlign = TextAlign.Center
    )

}

@Composable
fun InputFieldsSection(
    firstName: String,
    lastName: String,
    reEnterPassword: String,
    reEnterPasswordErrorMsg: String?,
    isReEnteredVisible: Boolean,
    passwordErrorMsg: String?,
    firstNameErrorMsg: String?,
    lastNameErrorMsg: String?,
    password: String,
    isPasswordVisible: Boolean,
    country: Country,
    email: String,
    emailErrorMsg: String?,
    phoneNo: String,
    showCountryChooser: Boolean,
    onEvent: (UserEvents) -> Unit
) {
    InputFieldWithError(placeholder = stringResource(Res.string.enter_first_name),
        text = firstName,
        errorMessage = firstNameErrorMsg,
        onTextChange = { onEvent(UserEvents.SetFirstName(it)) })

    InputFieldWithError(placeholder = stringResource(Res.string.enter_last_name),
        text = lastName,
        errorMessage = lastNameErrorMsg,
        onTextChange = { onEvent(UserEvents.SetLastName(it)) })

    PhoneWithCountrySelector(selectedCountry = country,
        phone = phoneNo,
        onPhoneNumberChange = { onEvent(UserEvents.SetPhoneNo(it)) },
        showCountryChooser = showCountryChooser,
        onCountryChange = { onEvent(UserEvents.OnCountryChange(it)) },
        toggleCountryPicker = { onEvent(UserEvents.ToggleCountryChooser) })

    InputFieldWithError(placeholder = stringResource(Res.string.enter_your_mail),
        text = email,
        errorMessage = emailErrorMsg,
        onTextChange = { onEvent(UserEvents.SetEmail(it)) })

    PasswordFieldWithError(placeholder = stringResource(Res.string.enter_your_password),
        text = password,
        errorMessage = passwordErrorMsg,
        isPasswordVisible = isPasswordVisible,
        onTogglePassword = { onEvent(UserEvents.TogglePasswordVisibility) },
        onTextChange = { onEvent(UserEvents.SetPassword(it)) })

    PasswordFieldWithError(placeholder = stringResource(Res.string.enter_your_password),
        text = reEnterPassword,
        errorMessage = reEnterPasswordErrorMsg,
        isPasswordVisible = isReEnteredVisible,
        onTogglePassword = { onEvent(UserEvents.ToggleReenteredPasswordVisibility) },
        onTextChange = { onEvent(UserEvents.OnRetypePassword(it)) })
}

@Composable
fun SignUpButtonSection(
    isSignUpEnabled: Boolean, isLoading: Boolean, onEvent: (UserEvents) -> Unit
) {
    AppRoundedCornerButton(
        onClick = { onEvent(UserEvents.Submit) }, isEnabled = isSignUpEnabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppTextLabel(
                text = stringResource(Res.string.login),
                color = if (isSignUpEnabled) Color.White else Color.DarkGray,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Show loading indicator only
            AnimatedVisibility(visible = isLoading) {
                Row {
                    Spacer(Modifier.weight(1f))
                    CircularProgressIndicator(
                        color = Color.White, modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AlreadyHaveAccountSection(loginScreen: () -> Unit) {
    AppTextLabel(text = stringResource(Res.string.already_have_an_account),
        fontSize = 12.sp,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable { loginScreen() })
}

@Composable
fun InputFieldWithError(
    placeholder: String, text: String, errorMessage: String?, onTextChange: (String) -> Unit
) {
    AppTextField(
        placeholder = placeholder,
        modifier = Modifier.fillMaxWidth(),
        text = text,
        errorMessage = errorMessage,
        isError = errorMessage != null,
        onTextChange = onTextChange
    )
}

@Composable
fun PasswordFieldWithError(
    placeholder: String,
    text: String,
    errorMessage: String?,
    isPasswordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onTextChange: (String) -> Unit
) {
    PasswordTextField(
        placeholder = placeholder,
        modifier = Modifier.fillMaxWidth(),
        text = text,
        errorMessage = errorMessage,
        isError = errorMessage != null,
        onTogglePassword = onTogglePassword,
        isPasswordVisible = isPasswordVisible,
        onTextChange = onTextChange
    )
}