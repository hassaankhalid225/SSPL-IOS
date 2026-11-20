package com.sspl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.theme.AppTheme

/*
 * Legacy button API kept for existing callers; visuals now come from the design system
 * (gradient PrimaryButton, LoadingDots instead of a spinner, theme colours for disabled state).
 */

@Composable
fun AppRoundedCornerButton(modifier: Modifier = Modifier, title: String, isEnabled: Boolean = true, onClick: () -> Unit) {
    PrimaryButton(
        text = title,
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp)
    )
}

@Composable
fun AppRoundedCornerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = AppTheme.colors
    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.buttonStart,
            contentColor = androidx.compose.ui.graphics.Color.White,
            disabledContainerColor = colors.boxColor,
            disabledContentColor = colors.muted
        ),
        shape = AppTheme.shapes.button,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, content = content)
    }
}

@Composable
fun ButtonWithProgress(
    isEnabled: Boolean,
    isLoading: Boolean,
    buttonText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PrimaryButton(
        text = buttonText,
        onClick = onClick,
        enabled = isEnabled,
        loading = isLoading,
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp)
    )
}
