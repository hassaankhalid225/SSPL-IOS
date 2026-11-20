package com.sspl.ui.settings.deleteaccount

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.EMAIL_GUEST_USER
import org.koin.compose.viewmodel.koinViewModel

/**
 * A9: Modernized Delete Account Screen with confirmation and warning states.
 */
@Composable
fun DeleteAccountScreen(
    navController: NavController,
    onParentGraph: () -> Unit = {},
    viewModel: DeleteAccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteAcRes) {
        uiState.deleteAcRes?.let {
            scaffoldState.showSnackbar(it.message)
            onParentGraph()
        }
    }

    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Delete Account Permanently?",
            message = "This action is irreversible. All your profile information, event registrations, abstract submissions, and certificates will be permanently deleted.",
            confirmText = "Delete Account",
            dismissText = "Cancel",
            onConfirm = {
                showConfirmDialog = false
                viewModel.deleteAccount()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        containerColor = AppTheme.colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.onPrimary
                        )
                    }
                    Text(
                        text = "Delete Account",
                        style = AppTheme.typography.titleMedium.copy(
                            color = AppTheme.colors.heading,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Warning Header Card
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AppTheme.colors.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Irreversible Action",
                                style = AppTheme.typography.titleMedium.copy(
                                    color = AppTheme.colors.error,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }

                        val isGuest = uiState.user?.account?.email?.contains(EMAIL_GUEST_USER) == true

                        if (isGuest) {
                            Text(
                                text = "You are currently signed in as a temporary guest user. You do not have a permanent account to delete.",
                                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            SecondaryButton(
                                text = "Exit Guest Session",
                                onClick = onParentGraph,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Deleting your account will permanently remove your member profile from the Society of Surgeons Pakistan (Lahore) portal.",
                                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body)
                            )

                            Text(
                                text = "What will be erased:",
                                style = AppTheme.typography.bodyMedium.copy(
                                    color = AppTheme.colors.heading,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "• Your personal and medical council KYC profile",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                                Text(
                                    text = "• Event registrations and attendance records",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                                Text(
                                    text = "• Abstract submissions and presentation files",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                                Text(
                                    text = "• Fellowship applications and logbook entries",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryButton(
                                text = "Delete My Account",
                                onClick = { showConfirmDialog = true },
                                isLoading = uiState.isLoading,
                                enabled = !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.error,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}
