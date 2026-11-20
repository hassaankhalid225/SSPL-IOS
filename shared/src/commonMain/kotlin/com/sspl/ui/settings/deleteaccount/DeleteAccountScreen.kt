package com.sspl.ui.settings.deleteaccount

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.DestructiveButton
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonLine
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.EMAIL_GUEST_USER
import org.koin.compose.viewmodel.koinViewModel

/**
 * Delete account (inside the main shell). Same flow as before: confirm -> DELETE account by email
 * -> session cleared by the use case -> leave to the auth graph.
 */
@Composable
fun DeleteAccountScreen(
    navController: NavController,
    onParentGraph: () -> Unit = {},
    viewModel: DeleteAccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteAcRes) {
        uiState.deleteAcRes?.let {
            snackbar.showSnackbar(it.message.ifBlank { "Your account has been deleted." })
            onParentGraph()
        }
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Delete account permanently?",
            message = "This can't be undone. Your profile, registrations, submissions and certificates will be removed.",
            confirmText = "Delete",
            dismissText = "Keep account",
            onConfirm = {
                showConfirmDialog = false
                viewModel.deleteAccount()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    val user = uiState.user
    val isGuest = user?.account?.email?.contains(EMAIL_GUEST_USER) == true
    val initialLoading = uiState.isLoading && user == null && uiState.deleteAcRes == null && uiState.error == null

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                initialLoading -> {
                    SkeletonBox(Modifier.fillMaxWidth().height(140.dp), AppTheme.shapes.card)
                    SkeletonLine(widthFraction = 0.7f, height = 14.dp)
                    SkeletonLine(widthFraction = 0.5f, height = 12.dp)
                }

                isGuest -> {
                    WarningCard(
                        title = "No permanent account",
                        message = "You're signed in as a temporary guest, so there's no account to delete. You can simply leave the guest session."
                    )
                    SecondaryButton(text = "Exit guest session", onClick = onParentGraph)
                }

                else -> {
                    WarningCard(
                        title = "This action is permanent",
                        message = "Deleting your account removes your member profile from the SSPL portal" +
                                (user?.account?.email?.let { " ($it)" } ?: "") + ". This cannot be undone."
                    )

                    uiState.error?.let { err ->
                        FeedbackBanner(
                            message = err.message.ifBlank { "Couldn't delete your account. Please try again." },
                            isError = true
                        )
                    }

                    SsplCard {
                        Text("What will be erased", style = AppTheme.typography.titleSmall)
                        Spacer(Modifier.size(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            EraseItem(Icons.Default.PersonOff, "Personal and professional profile")
                            EraseItem(Icons.Default.EventBusy, "Event registrations and attendance records")
                            EraseItem(Icons.AutoMirrored.Filled.Assignment, "Abstract submissions and presentations")
                            EraseItem(Icons.Default.School, "Fellowship applications and logbook entries")
                            EraseItem(Icons.Default.WorkspacePremium, "Certificates issued to you")
                        }
                    }

                    DestructiveButton(
                        text = "Delete my account",
                        onClick = { showConfirmDialog = true },
                        loading = uiState.isLoading,
                        enabled = user != null
                    )
                    OutlineButton(
                        text = "Keep my account",
                        onClick = { navController.navigateUp() },
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(12.dp))
    }

    if (uiState.isLoading && user != null) BlockingLoader("Deleting your account…")
}

@Composable
private fun WarningCard(title: String, message: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.errorContainer)
            .border(1.dp, colors.error.copy(alpha = 0.3f), AppTheme.shapes.card)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.WarningAmber,
            contentDescription = "Warning",
            tint = colors.error,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.titleMedium.copy(color = colors.error, fontWeight = FontWeight.Bold))
            Spacer(Modifier.size(4.dp))
            Text(message, style = AppTheme.typography.bodyMedium.copy(color = colors.body))
        }
    }
}

@Composable
private fun EraseItem(icon: ImageVector, text: String) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint = colors.error, background = colors.errorContainer, size = 32.dp, iconSize = 16.dp, corner = 10.dp)
        Spacer(Modifier.width(12.dp))
        Text(text, style = AppTheme.typography.bodyMedium.copy(color = colors.body), modifier = Modifier.weight(1f))
    }
}
