package com.sspl.ui.settings

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.auth.RoleUtils
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ThemeModeSwitch
import com.sspl.designsystem.theme.AppTheme
import com.sspl.designsystem.theme.ThemePreferences
import com.sspl.session.UserSession
import com.sspl.core.usecases.LogoutUseCase
import com.sspl.designsystem.components.BlockingLoader
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Settings (inside the main shell).
 *
 * @param onParentGraph leaves the main graph for the auth graph (sign-in). The shell should pass
 *   it (like Profile / DeleteAccount). If it is not provided, sign-out still clears the session and
 *   lands on the Profile tab, whose guest view offers "Sign in".
 * @param onResetPassword opens the forgot/reset-password flow, which lives in the auth graph.
 *   The row is hidden when the host doesn't provide it (navigating to that route from the inner
 *   NavController would crash — it isn't registered there).
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    userSession: UserSession = koinInject(),
    themePreferences: ThemePreferences = koinInject(),
    logoutUseCase: LogoutUseCase = koinInject(),
    onParentGraph: (() -> Unit)? = null,
    onResetPassword: (() -> Unit)? = null
) {
    val user by userSession.userFlow.collectAsStateWithLifecycle()
    val themeMode by themePreferences.mode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var signingOut by remember { mutableStateOf(false) }
    val isGuest = user == null || user?.isGuest() == true

    val leaveToSignIn: () -> Unit = {
        if (onParentGraph != null) onParentGraph()
        else navController.navigate(Screen.ProfileScreen.route) {
            popUpTo(Screen.HomeScreen.route)
            launchSingleTop = true
        }
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = "Sign out?",
            message = "You'll need to sign in again to access your member portal.",
            confirmText = "Sign out",
            dismissText = "Cancel",
            onConfirm = {
                showSignOutDialog = false
                if (!signingOut) {
                    signingOut = true
                    scope.launch {
                        // Best-effort POST /auth/logout, then the local session is always cleared.
                        logoutUseCase()
                        signingOut = false
                        leaveToSignIn()
                    }
                }
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Account summary
        val u = user
        AccountSummary(
            title = if (isGuest) "Guest" else RoleUtils.getUserDisplayName(u),
            subtitle = if (isGuest) "Sign in to manage your account" else (u?.account?.email ?: RoleUtils.getMembershipLabel(u)),
            avatarUrl = if (isGuest) null else RoleUtils.getProfileImageUrl(u?.profilePicture).takeIf { it.isNotBlank() },
            initials = if (isGuest) "G" else RoleUtils.getUserInitials(u),
            onClick = { if (isGuest) leaveToSignIn() else navController.navigate(Screen.ProfileScreen.route) }
        )

        SettingsGroup("Appearance") {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(SsplIcons.Palette, size = 38.dp, iconSize = 18.dp, corner = 12.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Theme", style = AppTheme.typography.titleSmall)
                    Text("Follow the system or pick light / dark", style = AppTheme.typography.bodySmall)
                }
            }
            ThemeModeSwitch(
                selected = themeMode,
                onSelect = themePreferences::setMode,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
            )
        }

        SettingsGroup("Updates") {
            SettingsRow(
                icon = SsplIcons.Campaign,
                title = "Announcements",
                subtitle = "News, notices and event updates",
                onClick = { navController.navigate(Screen.NotificationsScreen.route) }
            )
        }

        if (!isGuest) {
            SettingsGroup("Account") {
                if (onResetPassword != null) {
                    SettingsRow(
                        icon = SsplIcons.LockReset,
                        title = "Reset password",
                        subtitle = "Get a code by email and set a new password",
                        onClick = onResetPassword
                    )
                    RowDivider()
                }
                SettingsRow(
                    icon = SsplIcons.DeleteForever,
                    title = "Delete account",
                    subtitle = "Permanently remove your account and data",
                    destructive = true,
                    onClick = { navController.navigate(Screen.DeleteAccountScreen.route) }
                )
            }
        }

        SettingsGroup("About & help") {
            SettingsRow(SsplIcons.Info, "About SSPL", "Mission, leadership and history") {
                navController.navigate(Screen.AboutScreen.route)
            }
            RowDivider()
            SettingsRow(SsplIcons.HelpOutline, "FAQs", "Answers to common questions") {
                navController.navigate(Screen.FaqsScreen.route)
            }
            RowDivider()
            SettingsRow(SsplIcons.SupportAgent, "Contact us", "Reach the SSPL secretariat") {
                navController.navigate(Screen.ContactUsScreen.route)
            }
            RowDivider()
            SettingsRow(SsplIcons.Description, "Terms & conditions", "Society policies and membership terms") {
                navController.navigate(Screen.TermsScreen.route)
            }
        }

        SettingsGroup(null) {
            if (isGuest) {
                SettingsRow(
                    icon = SsplIcons.Login,
                    title = "Sign in",
                    subtitle = "Access your member portal",
                    onClick = leaveToSignIn
                )
            } else {
                SettingsRow(
                    icon = SsplIcons.Logout,
                    title = "Sign out",
                    subtitle = "Sign out of this device",
                    destructive = true,
                    showChevron = false,
                    onClick = { showSignOutDialog = true }
                )
            }
        }

        Text(
            text = "Society of Surgeons Pakistan, Lahore",
            style = AppTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
    }
    if (signingOut) BlockingLoader("Signing out…")
}

@Composable
private fun AccountSummary(
    title: String,
    subtitle: String,
    avatarUrl: String?,
    initials: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.headerGradient)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            url = avatarUrl,
            initials = initials,
            size = 52.dp,
            modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.55f), androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.titleMedium.copy(color = Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.75f)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            SsplIcons.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SettingsGroup(title: String?, content: @Composable ColumnScope.() -> Unit) {
    val colors = AppTheme.colors
    Column {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = AppTheme.typography.overline,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.card)
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card),
            content = content
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(Modifier.padding(start = 64.dp), color = AppTheme.colors.divider)
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    destructive: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(
            icon,
            tint = if (destructive) colors.error else colors.primary,
            background = if (destructive) colors.errorContainer else colors.primaryContainer,
            size = 38.dp, iconSize = 18.dp, corner = 12.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = AppTheme.typography.titleSmall.copy(color = if (destructive) colors.error else colors.heading)
            )
            Text(subtitle, style = AppTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (showChevron) {
            Icon(
                SsplIcons.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
