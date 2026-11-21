package com.sspl.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme

/**
 * Labelled checkbox row (e.g. "Remember me"). The whole row is the 48dp touch target.
 */
@Composable
fun SsplCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = AppTheme.colors.buttonStart,
                uncheckedColor = AppTheme.colors.muted,
                checkmarkColor = androidx.compose.ui.graphics.Color.White
            )
        )
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
