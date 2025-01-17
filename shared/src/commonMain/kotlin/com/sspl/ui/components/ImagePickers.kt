package com.sspl.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun CameraPermissionDeniedDialog(
    onDismiss: () -> Unit, onOpenSettings: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = "Camera Permission Required", style = MaterialTheme.typography.titleLarge
        )
    }, text = {
        Column {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera permission is required to use this feature. Please enable camera access in your device settings.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }, confirmButton = {
        TextButton(onClick = onOpenSettings) {
            Text("Open Settings")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Dismiss")
        }
    })
}
