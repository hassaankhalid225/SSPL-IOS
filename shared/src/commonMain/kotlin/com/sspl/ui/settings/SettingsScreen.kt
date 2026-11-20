package com.sspl.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import org.koin.compose.koinInject

/**
 * A8: Modernized Settings Screen with Notifications toggle, account management, legal & sign out.
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    userSession: UserSession = koinInject()
) {
    val user by userSession.userFlow.collectAsStateWithLifecycle(initialValue = null)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            confirmText = "Sign Out",
            dismissText = "Cancel",
            onConfirm = {
                showSignOutDialog = false
                scope.launch {
                    userSession.resetToken()
                    userSession.resetUser()
                    navController.navigate(Screen.SignInScreen.route) {
                        popUpTo(Screen.MainScreen.route) { inclusive = true }
                    }
                }
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    Scaffold(
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
                        text = "Settings",
                        style = AppTheme.typography.titleMedium.copy(
                            color = AppTheme.colors.heading,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Account Section
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SectionHeader(title = "Account")

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsRow(
                            icon = Icons.Default.Person,
                            title = "Profile & Credentials",
                            subtitle = user?.fullName() ?: "Manage your profile",
                            onClick = { navController.navigate(Screen.ProfileScreen.route) }
                        )

                        HorizontalDivider(color = AppTheme.colors.border)

                        SettingsRow(
                            icon = Icons.Default.LockReset,
                            title = "Reset Password",
                            subtitle = "Update your security credentials",
                            onClick = { navController.navigate(Screen.ForgotPasswordScreen.route) }
                        )

                        HorizontalDivider(color = AppTheme.colors.border)

                        SettingsRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete Account",
                            subtitle = "Permanently close and erase your account",
                            titleColor = AppTheme.colors.error,
                            onClick = { navController.navigate(Screen.DeleteAccountScreen.route) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Preferences Section
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SectionHeader(title = "Preferences")

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = AppTheme.colors.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.padding(start = 14.dp)) {
                                    Text(
                                        text = "Push Notifications",
                                        style = AppTheme.typography.bodyLarge.copy(
                                            color = AppTheme.colors.heading,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Conference updates, CMEs & news",
                                        style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                    )
                                }
                            }

                            Switch(
                                checked = pushNotificationsEnabled,
                                onCheckedChange = { pushNotificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppTheme.colors.background,
                                    checkedTrackColor = AppTheme.colors.secondary,
                                    uncheckedThumbColor = AppTheme.colors.muted,
                                    uncheckedTrackColor = AppTheme.colors.surface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Legal & About Section
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SectionHeader(title = "About & Legal")

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "About SSPL",
                            subtitle = "Mission, vision, leadership & history",
                            onClick = { navController.navigate(Screen.AboutScreen.route) }
                        )

                        HorizontalDivider(color = AppTheme.colors.border)

                        SettingsRow(
                            icon = Icons.Default.Description,
                            title = "Terms & Conditions",
                            subtitle = "Society policies and membership terms",
                            onClick = { /* Nav to Terms */ }
                        )

                        HorizontalDivider(color = AppTheme.colors.border)

                        SettingsRow(
                            icon = Icons.Default.Shield,
                            title = "Privacy Policy",
                            subtitle = "Data security and member confidentiality",
                            onClick = { /* Nav to Privacy */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Out Button
                SecondaryButton(
                    text = "Sign Out",
                    onClick = { showSignOutDialog = true },
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // App Version info
                Text(
                    text = "Society of Surgeons Pakistan (Lahore)\nVersion 1.0.0 (Build 2026.11)",
                    style = AppTheme.typography.labelSmall.copy(
                        color = AppTheme.colors.muted.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = AppTheme.colors.heading
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor != AppTheme.colors.heading) titleColor else AppTheme.colors.secondary,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = title,
                    style = AppTheme.typography.bodyLarge.copy(
                        color = titleColor,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.muted,
            modifier = Modifier.size(20.dp)
        )
    }
}
