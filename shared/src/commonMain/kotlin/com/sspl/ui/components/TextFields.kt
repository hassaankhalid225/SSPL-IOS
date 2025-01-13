package com.sspl.ui.components
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.sharp.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.theme.boxColor
@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    text: String?,
    placeholder: String = "Enter your text here...",
    shape: Shape = RoundedCornerShape(22),
    onFocusLost: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChange: (String) -> Unit,
    errorMessage: String? = null
) {
    BaseOutlinedTextField(
        modifier = modifier,
        text = text,
        placeholder = placeholder,
        shape = shape,
        onFocusLost = onFocusLost,
        keyboardType = keyboardType,
        onTextChange = onTextChange,
        errorMessage = errorMessage
    )
}

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    text: String?,
    placeholder: String = "Enter your password...",
    shape: Shape = RoundedCornerShape(22),
    isPasswordVisible: Boolean = false,
    onFocusLost: () -> Unit = {},
    onTogglePassword: () -> Unit,
    onTextChange: (String) -> Unit,
    errorMessage: String? = null
) {
    BaseOutlinedTextField(
        modifier = modifier,
        text = text,
        placeholder = placeholder,
        shape = shape,
        onFocusLost = onFocusLost,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        isPasswordVisible = isPasswordVisible,
        onTogglePassword = onTogglePassword,
        onTextChange = onTextChange,
        errorMessage = errorMessage
    )
}


@Composable
private fun BaseTextField(
    modifier: Modifier = Modifier,
    text: String?,
    placeholder: String,
    shape: Shape,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},onFocusLost: () -> Unit={},
    onTextChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    TextField(
        value = text ?: "",
        onValueChange = { onTextChange(it) },
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
                if (isFocused && !focusState.isFocused) {
                    // Focus lost
                    onFocusLost()
                }

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
        shape = shape,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Favorite else Icons.Sharp.Call,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null,
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
            onFocusLost()
        }),
    )
}
@Composable
private fun BaseOutlinedTextField(
    modifier: Modifier = Modifier,
    text: String?,
    placeholder: String,
    shape: Shape,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChange: (String) -> Unit,
    errorMessage: String? = null,
    isError: Boolean = errorMessage!=null
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text ?: "",
        onValueChange = { onTextChange(it) },
        visualTransformation = when {
            isPassword && !isPasswordVisible -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                fontFamily = FontFamily.Default
            )
        },
        modifier = modifier
            .onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused) {
                    onFocusLost()
                }
                isFocused = focusState.isFocused
            },
        maxLines = 6,
        singleLine = false,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        shape = shape,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Call else Icons.Default.Call,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black.copy(0.7f),
            focusedBorderColor = if (isError) Color.Red else Color.Gray,
            unfocusedBorderColor = if (isError) Color.Red else Color.LightGray,
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onFocusLost()
        }),
        isError = isError,
        supportingText = if (isError && !errorMessage.isNullOrBlank()) {
            { Text(errorMessage, color = Color.Red) }
        } else null
    )
}
