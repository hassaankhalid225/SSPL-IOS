package com.sspl.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sspl.core.auth.RoleUtils
import com.sspl.designsystem.components.GuestLockSheet
import com.sspl.designsystem.components.SsplBottomBar
import com.sspl.designsystem.components.SsplDrawerContent
import com.sspl.designsystem.components.SsplTopBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import kotlinx.coroutines.launch

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
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showGuestLockSheet by remember { mutableStateOf(false) }

    val user by userSession.userFlow.collectAsState()
    val isGuest = user == null || user?.isGuest() == true
    val avatarUrl = RoleUtils.getProfileImageUrl(user?.profilePicture)
    val initials = RoleUtils.getUserInitials(user)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !canNavigateBack,
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
                    onLogout()
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = AppTheme.colors.background,
            topBar = {
                SsplTopBar(
                    title = title,
                    isHomeScreen = currentRoute is Route.Home,
                    canNavigateBack = canNavigateBack,
                    onNavigationClick = {
                        if (canNavigateBack) {
                            onNavigateBack()
                        } else {
                            scope.launch { drawerState.open() }
                        }
                    },
                    unreadNotificationCount = unreadNotificationCount,
                    onNotificationsClick = onNotificationsClick,
                    avatarUrl = avatarUrl,
                    userInitials = initials,
                    onAvatarClick = {
                        if (isGuest) {
                            showGuestLockSheet = true
                        } else {
                            onNavigate(Route.Profile)
                        }
                    }
                )
            },
            bottomBar = {
                SsplBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == BottomBarTab.MORE) {
                            scope.launch { drawerState.open() }
                        } else {
                            onTabSelected(tab)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                content(Modifier.fillMaxSize())
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
}

