package com.sspl.ui.components
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sspl.resources.Res
import com.sspl.resources.no_visibility_icon
import com.sspl.resources.visibility_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String = "Enter your text here...",
    shape: Shape = RoundedCornerShape(8.dp),
    onFocusLost: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = text,
        onValueChange = {newText ->
            val trimmedText = newText.replace("\\s".toRegex(), "")
            onTextChange(trimmedText)
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (!it.isFocused) onFocusLost() },
        label = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall
            )
        },
        shape = shape,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = singleLine,
        isError = isError,
        supportingText = if (isError && !errorMessage.isNullOrBlank()) {
            { Text(text = errorMessage) }
        } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String = "Enter your password...",
    shape: Shape = RoundedCornerShape(8.dp),
    isPasswordVisible: Boolean = false,
    onFocusLost: () -> Unit = {},
    onTogglePassword: () -> Unit,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = {newText ->
            val trimmedText = newText.replace("\\s".toRegex(), "")
            onTextChange(trimmedText)
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (!it.isFocused) onFocusLost() },
        label = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall
            )
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = true, shape = shape,
        isError = isError,
        supportingText = if (isError && !errorMessage.isNullOrBlank()) {
            { Text(text = errorMessage) }
        } else null,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onTogglePassword) {
                Icon(
                    painter =  painterResource(if (isPasswordVisible) Res.drawable.visibility_icon else Res.drawable.no_visibility_icon),
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

