package com.sspl.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sspl.core.auth.RoleUtils
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.GuestLockSheet
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.ShellActions
import com.sspl.designsystem.components.SsplBottomBar
import com.sspl.designsystem.components.SsplDrawerContent
import com.sspl.designsystem.components.SsplTopBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.designsystem.theme.SystemBarsAppearance
import com.sspl.session.UserSession
import kotlinx.coroutines.launch

/**
 * Main shell: gradient top bar + floating bottom bar + grouped drawer.
 *
 * Insets: the Scaffold consumes none (contentWindowInsets = 0). The top bar pads for the
 * status bar and the bottom bar pads for the navigation bar themselves, so the gradient fills
 * the status bar area and there is no empty band.
 *
 * Home draws its own hero header, so the shared top bar is hidden there; Home reads
 * [LocalShellActions] for menu / notifications / avatar.
 */
@Composable
fun MainScaffold(
    currentRoute: Route,
    currentTab: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit,
    onNavigate: (Route) -> Unit,
    onNavigateBack: () -> Unit,
    canNavigateBack: Boolean,
    title: String,
    userSession: UserSession,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    unreadNotificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    /** True only for the Home destination (which draws its own hero header). */
    isHomeScreen: Boolean = currentRoute is Route.Home,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showGuestLockSheet by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    val user by userSession.userFlow.collectAsState()
    val isGuest = user == null || user?.isGuest() == true
    val avatarUrl = RoleUtils.getProfileImageUrl(user?.profilePicture)
    val initials = RoleUtils.getUserInitials(user)
    val isHome = isHomeScreen

    // Status bar icons are always light: every main screen has a navy gradient behind them.
    SystemBarsAppearance(lightStatusIcons = true, darkTheme = AppTheme.isDark)

    val signOut: () -> Unit = {
        scope.launch {
            // Clear token, refresh token and cached user so the next launch starts at sign-in.
            userSession.resetUser()
            onLogout()
        }
    }

    val shellActions = ShellActions(
        openDrawer = { scope.launch { drawerState.open() } },
        onNotifications = { onNotificationsClick?.invoke() },
        onAvatar = { if (isGuest) showGuestLockSheet = true else onNavigate(Route.Profile) },
        unreadCount = unreadNotificationCount,
        avatarUrl = avatarUrl,
        initials = initials,
        displayName = if (isGuest) null else RoleUtils.getUserDisplayName(user),
        isGuest = isGuest
    )

    CompositionLocalProvider(LocalShellActions provides shellActions) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !canNavigateBack || drawerState.isOpen,
            drawerContent = {
                SsplDrawerContent(
                    user = user,
                    isGuest = isGuest,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        onNavigate(route)
                    },
                    onSignIn = {
                        scope.launch { drawerState.close() }
                        onSignIn()
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        confirmLogout = true
                    },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = AppTheme.colors.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    AnimatedVisibility(
                        visible = !isHome,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        SsplTopBar(
                            title = title,
                            canNavigateBack = canNavigateBack,
                            onNavigationClick = {
                                if (canNavigateBack) onNavigateBack()
                                else scope.launch { drawerState.open() }
                            },
                            unreadNotificationCount = unreadNotificationCount,
                            onNotificationsClick = onNotificationsClick,
                            avatarUrl = avatarUrl,
                            userInitials = initials,
                            onAvatarClick = shellActions.onAvatar
                        )
                    }
                },
                bottomBar = {
                    SsplBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            if (tab == BottomBarTab.MORE) scope.launch { drawerState.open() }
                            else onTabSelected(tab)
                        }
                    )
                }
            ) { paddingValues ->
                // The shell's bars already own the system insets, so consume them for content:
                // any navigationBarsPadding()/statusBarsPadding() inside a screen becomes 0 here
                // (no double gaps above sticky CTAs). Home keeps the status inset for its hero.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(
                            if (isHome) WindowInsets.navigationBars else WindowInsets.systemBars
                        )
                ) {
                    content(Modifier.fillMaxSize())
                }
            }
        }
    }

    if (showGuestLockSheet) {
        GuestLockSheet(
            onSignIn = {
                showGuestLockSheet = false
                onSignIn()
            },
            onDismiss = { showGuestLockSheet = false }
        )
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Sign out?",
            message = "You'll need your email and password to sign in again.",
            confirmText = "Sign out",
            onConfirm = {
                confirmLogout = false
                signOut()
            },
            onDismiss = { confirmLogout = false }
        )
    }
}
