package com.sspl.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.eygraber.compose.country.code.picker.CountryCodePicker
import com.sspl.theme.boxColor
import com.vanniktech.locale.Country

@Composable
fun PhoneWithCountrySelector(modifier: Modifier = Modifier, selectedCountry: Country, phone: String, onPhoneNumberChange: (String) -> kotlin.Unit, showCountryChooser: Boolean, onCountryChange:(Country)->Unit, toggleCountryPicker: () -> Unit) {
    Row(modifier) {
        androidx.compose.animation.AnimatedVisibility(showCountryChooser) {
            Box(
                modifier = Modifier
                    .height(230.dp).fillMaxWidth()
            ) {
                Column  (
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CountryCodePicker(
                        initialCountry = Country.PAKISTAN,
                        onClick = { country ->
                            onCountryChange.invoke(country)
                        },
                    )
                }
            }
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