package com.sspl.ui.kyc


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.theme.boxColor
import com.sspl.ui.components.AppRoundedCornerButton
import com.sspl.ui.components.AppTextBody
import com.sspl.ui.components.AppTextTitle
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


@Preview
@Composable
fun SignUpScreen(
    navController: NavController, viewModel: SignUpViewModel = koinViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .background(Color.White).padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTextTitle("Blood Pressure", modifier = Modifier.align(Alignment.CenterHorizontally))
        AppTextTitle("Fill out all the Values")
        FormTextField(text = "",
            placeholder = "Enter First Name",
            onTextChange = { viewModel.onEvent(UserEvents.SetFirstName(it)) })
        FormTextField(text = "",
            placeholder = "Enter Last Name",
            onTextChange = { viewModel.onEvent(UserEvents.SetLastName(it)) })
        FormTextField(text = "",
            placeholder = "Enter Phone",
            onTextChange = { viewModel.onEvent(UserEvents.SetPhoneNo(it)) })
        FormTextField(text = "",
            placeholder = "Enter Email",
            onTextChange = { viewModel.onEvent(UserEvents.SetEmail(it)) })
        FormTextField(text = "",
            placeholder = "Enter Password",
            onTextChange = { viewModel.onEvent(UserEvents.SetPassword(it)) })
        FormTextField(text = "",
            placeholder = "Retype Password",
            onTextChange = { viewModel.onEvent(UserEvents.OnRetypePassword(it)) })
        AppRoundedCornerButton(title = "Sign Up",
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.onEvent(UserEvents.Submit) })
    }
}


@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    text: String? = "",
    placeholder: String = "Enter your text here...",
    shape: Shape = RoundedCornerShape(22),
    keyboardType: KeyboardType = KeyboardType.Text,
    onFocusLost: () -> Unit = {},
    onTextChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    TextField(
        value = text ?: "",
        onValueChange = { onTextChange.invoke(it) },
        placeholder = {
            AppTextBody(
                placeholder
            )
        },
        modifier = modifier.fillMaxWidth().onFocusChanged { focusState ->
            if (isFocused && !focusState.isFocused) {
                // Focus was lost
                onFocusLost()
            }
            isFocused = focusState.isFocused
        }.border(
            width = 1.dp, color = if (!isFocused) boxColor else Color.LightGray, shape = shape
        ),
        maxLines = 6,
        singleLine = false,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done, keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(22),
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
            onFocusLost() // Also trigger the callback when "Done" is pressed
        }),
    )
}

