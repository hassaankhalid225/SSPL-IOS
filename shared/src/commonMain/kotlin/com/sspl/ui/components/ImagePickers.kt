package com.sspl.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.PeekabooCameraState
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import com.sspl.resources.Res
import com.sspl.resources.capture_icon
import com.sspl.resources.switch_camera
import org.jetbrains.compose.resources.painterResource


@Composable
fun AppCameraView(onImageCaptured: (bytes: ByteArray?) -> Unit) {
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val state = rememberPeekabooCameraState(onCapture = { bytes: ByteArray? ->
        onImageCaptured(bytes)
    })

    Box(modifier = Modifier.fillMaxSize()) {
        PeekabooCamera(state = state, modifier = Modifier.fillMaxSize(), permissionDeniedContent = {
            showPermissionDeniedDialog = true
        })

        CameraOverlay(
            state = state, modifier = Modifier.fillMaxSize(), onCloseClick = {
                onImageCaptured(null)
            }
        )
    }

    if (showPermissionDeniedDialog) {
        CameraPermissionDeniedDialog(onDismiss = {
            showPermissionDeniedDialog = false
            onImageCaptured(null)
        }, onOpenSettings = {
            onImageCaptured(null)
            showPermissionDeniedDialog = false
            // OpenAppSettings()
        })
    }
}
@Composable
fun CameraOverlay(
    state: PeekabooCameraState,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Close button at the top
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close camera",
                tint = Color.White
            )
        }

        // Bottom row with capture and toggle buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer for left alignment
            Spacer(modifier = Modifier.width(72.dp))

            // Capture button
            IconButton(
                onClick = { state.capture() },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Image(
                    painter = painterResource(Res.drawable.capture_icon),
                    contentDescription = "Capture photo",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                )
            }

            // Switch camera button
            IconButton(
                onClick = { state.toggleCamera() },
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    .padding(6.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.switch_camera),
                    contentDescription = "Switch camera",
                    tint = Color.White
                )
            }
        }
    }
}

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
