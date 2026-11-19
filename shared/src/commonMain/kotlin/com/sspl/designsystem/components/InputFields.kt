package com.sspl.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.designsystem.theme.AppTheme

@Composable
fun SsplTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    errorMessage: String? = null,
    isError: Boolean = false,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    leadingIconContent: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val effectiveError = error ?: errorMessage
    val effectiveIsError = isError || effectiveError != null
    val effectiveLeadingIcon: @Composable (() -> Unit)? = leadingIconContent ?: leadingIcon?.let { icon ->
        {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (effectiveIsError) AppTheme.colors.error else AppTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = AppTheme.typography.labelMedium.copy(
                    color = if (effectiveIsError) AppTheme.colors.error else AppTheme.colors.heading,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)) }
            } else null,
            leadingIcon = effectiveLeadingIcon,
            trailingIcon = trailingIcon,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            isError = effectiveIsError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            keyboardActions = keyboardActions,
            shape = AppTheme.shapes.button,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTheme.colors.primary,
                unfocusedBorderColor = AppTheme.colors.border,
                errorBorderColor = AppTheme.colors.error,
                focusedContainerColor = AppTheme.colors.surface,
                unfocusedContainerColor = AppTheme.colors.surface,
                errorContainerColor = AppTheme.colors.surface,
                focusedTextColor = AppTheme.colors.heading,
                unfocusedTextColor = AppTheme.colors.body
            ),
            textStyle = AppTheme.typography.bodyLarge
        )
        AnimatedVisibility(visible = effectiveError != null) {
            Text(
                text = effectiveError.orEmpty(),
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.error
                ),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
    placeholder: String = "••••••••",
    error: String? = null,
    errorMessage: String? = null,
    isError: Boolean = false,
    isVisible: Boolean? = null,
    onToggleVisibility: (() -> Unit)? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var internalVisible by remember { mutableStateOf(false) }
    val passwordVisible = isVisible ?: internalVisible
    val toggle = onToggleVisibility ?: { internalVisible = !internalVisible }
    val effectiveError = error ?: errorMessage

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = AppTheme.typography.labelMedium.copy(
                color = if (effectiveError != null) AppTheme.colors.error else AppTheme.colors.heading,
                fontWeight = FontWeight.Medium
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)) },
            enabled = enabled,
            singleLine = true,
            isError = effectiveError != null || isError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            keyboardActions = keyboardActions,
            shape = AppTheme.shapes.button,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTheme.colors.primary,
                unfocusedBorderColor = AppTheme.colors.border,
                errorBorderColor = AppTheme.colors.error,
                focusedContainerColor = AppTheme.colors.surface,
                unfocusedContainerColor = AppTheme.colors.surface,
                errorContainerColor = AppTheme.colors.surface,
                focusedTextColor = AppTheme.colors.heading,
                unfocusedTextColor = AppTheme.colors.body
            ),
            textStyle = AppTheme.typography.bodyLarge,
            trailingIcon = {
                IconButton(onClick = toggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = AppTheme.colors.muted
                    )
                }
            }
        )
        AnimatedVisibility(visible = effectiveError != null) {
            Text(
                text = effectiveError.orEmpty(),
                style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.error),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.chip)
            .background(AppTheme.colors.boxColor)
            .border(1.dp, AppTheme.colors.border, AppTheme.shapes.chip)
            .padding(horizontal = 14.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AppTheme.colors.muted,
                modifier = Modifier.size(20.dp)
            )
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty() && onClear != null) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    tint = AppTheme.colors.muted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onClear)
                )
            }
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    stepLabels: List<String>? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.screenHorizontal, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step < currentStep
                val isCurrent = step == currentStep
                val circleBg = when {
                    isCompleted -> AppTheme.colors.success
                    isCurrent -> AppTheme.colors.primary
                    else -> AppTheme.colors.border
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(circleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$step",
                        style = AppTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                if (step < totalSteps) {
                    val lineColor = if (step < currentStep) AppTheme.colors.success else AppTheme.colors.border
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                            .background(lineColor)
                    )
                }
            }
        }

        if (stepLabels != null && currentStep in 1..stepLabels.size) {
            Text(
                text = "Step $currentStep of $totalSteps: ${stepLabels[currentStep - 1]}",
                style = AppTheme.typography.labelMedium.copy(
                    color = AppTheme.colors.primaryDark,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

