package com.sspl.ui.kyc


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.resources.Res
import com.sspl.resources.enter_your_password
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppTextField
import com.sspl.ui.components.AppTextTitle
import com.sspl.ui.components.PasswordTextField
import com.sspl.ui.components.PhoneWithCountrySelector
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


@Preview
@Composable
fun SignUpScreen(
    navController: NavController, viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .background(Color.White).padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTextTitle("Sign up", modifier = Modifier.align(Alignment.CenterHorizontally))
        AppTextTitle("Fill out all the Values")
        AppTextField(text = uiState.firstName,
            placeholder = "Enter First Name",
            onTextChange = { viewModel.onEvent(UserEvents.SetFirstName(it)) })
        AppTextField(text = uiState.lastName,
            placeholder = "Enter Last Name",
            onTextChange = { viewModel.onEvent(UserEvents.SetLastName(it)) })
        PhoneWithCountrySelector(
            selectedCountry = uiState.country,
            phone = uiState.phoneNo,
            onPhoneNumberChange = {
                viewModel.onEvent(UserEvents.SetPhoneNo(it))
            },
            showCountryChooser = uiState.showCountryChooser,
            onCountryChange = {
                viewModel.onEvent(UserEvents.OnCountryChange(it))
            },
            toggleCountryPicker = {
                viewModel.onEvent(UserEvents.ToggleCountryChooser)}
        )
        AppTextField(text = "",
            placeholder = "Enter Email",
            onTextChange = { viewModel.onEvent(UserEvents.SetEmail(it)) })
        PasswordTextField(
            placeholder = stringResource(Res.string.enter_your_password),
            modifier = Modifier.fillMaxWidth(),
            text = uiState.password,
            onTogglePassword = {
                viewModel.onEvent(UserEvents.TogglePasswordVisibility)
            },
            isPasswordVisible = uiState.isPasswordVisible,
            onTextChange = { viewModel.onEvent(UserEvents.SetPassword(it)) })

        PasswordTextField(
            placeholder = "Re-enter Password",
            modifier = Modifier.fillMaxWidth(),
            text = uiState.password,
            onTogglePassword = {
                viewModel.onEvent(UserEvents.TogglePasswordVisibility)
            },
            isPasswordVisible = uiState.isPasswordVisible,
            onTextChange = { viewModel.onEvent(UserEvents.SetPassword(it)) })
        AppRoundedCornerButton(title = "Sign Up",
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.onEvent(UserEvents.Submit) })
    }
}

