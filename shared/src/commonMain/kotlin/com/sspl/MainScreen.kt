package com.sspl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sspl.core.repositories.NotificationRepository
import com.sspl.navigation.BottomBarTab
import com.sspl.navigation.MainScaffold
import com.sspl.navigation.Route
import com.sspl.session.UserSession
import com.sspl.ui.settings.deleteaccount.DeleteAccountScreen
import com.sspl.ui.userdetails.ProfileScreen
import org.koin.compose.koinInject

@Composable
fun MainScreen(onParentGraph: () -> Unit) {
    val navController = rememberNavController()
    val userSession: UserSession = koinInject()
    val notificationRepository: NotificationRepository = koinInject()
    val user by userSession.userFlow.collectAsState()
    val unreadNotifications by notificationRepository.unreadCount.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteStr = backStackEntry?.destination?.route
    val currentScreen = screenByRoute(currentRouteStr) ?: Screen.HomeScreen
    val canNavigateBack = navController.previousBackStackEntry != null

    val currentTab = when (currentScreen) {
        Screen.HomeScreen -> BottomBarTab.HOME
        Screen.ScientificProgramsScreen, Screen.WorkshopsScreen -> BottomBarTab.EVENTS
        Screen.FellowshipHubScreen -> BottomBarTab.FELLOWSHIP
        Screen.ProfileScreen, Screen.UserDetailsScreen, Screen.EditProfile -> BottomBarTab.MY_PORTAL
        else -> BottomBarTab.HOME
    }

    val currentRoute: Route = when (currentScreen) {
        Screen.HomeScreen -> Route.Home
        Screen.AboutScreen -> Route.About
        Screen.HistoryScreen -> Route.History
        Screen.ExecutiveCouncilScreen -> Route.ExecutiveCouncil
        Screen.ElectionsScreen -> Route.Elections
        Screen.NotificationsScreen -> Route.News
        Screen.FaqsScreen -> Route.Faqs
        Screen.MemberGuideScreen -> Route.MemberGuide
        Screen.ContactUsScreen -> Route.Contact
        Screen.TermsScreen -> Route.Terms
        Screen.DownloadsScreen -> Route.Downloads
        Screen.VerifyCertificateScreen -> Route.VerifyCertificate()
        Screen.MembersDirectoryScreen -> Route.MembersDirectory
        Screen.ScientificProgramsScreen -> Route.Events
        Screen.EventsListScreen -> Route.Events
        Screen.EventDetailScreen -> Route.Events
        Screen.EventRegistrationScreen -> Route.Events
        Screen.ConferencesScreen -> Route.Conferences
        Screen.EventArchiveScreen -> Route.Events
        Screen.WorkshopsScreen -> Route.Workshops()
        Screen.ProfileScreen -> Route.Profile
        Screen.SettingsScreen -> Route.Settings
        Screen.DeleteAccountScreen -> Route.DeleteAccount
        Screen.MyRegistrationsScreen -> Route.MyRegistrations
        else -> Route.Home
    }

    val screenTitle = when (currentScreen) {
        Screen.HomeScreen -> "SSP Lahore"
        Screen.AboutScreen -> "About Us"
        Screen.HistoryScreen -> "Society History"
        Screen.ExecutiveCouncilScreen -> "Executive Council"
        Screen.ElectionsScreen -> "Elections"
        Screen.NotificationsScreen -> "Announcements"
        Screen.FaqsScreen -> "FAQs"
        Screen.MemberGuideScreen -> "Member Guide"
        Screen.ContactUsScreen -> "Contact Us"
        Screen.TermsScreen -> "Terms & Conditions"
        Screen.DownloadsScreen -> "Downloads"
        Screen.VerifyCertificateScreen -> "Verify Certificate"
        Screen.MembersDirectoryScreen -> "Members Directory"
        Screen.ScientificProgramsScreen -> "Events & CME"
        Screen.EventsListScreen -> "Events & CME"
        Screen.EventDetailScreen -> "Event Details"
        Screen.EventRegistrationScreen -> "Registration"
        Screen.ConferencesScreen -> "Annual Conferences"
        Screen.EventArchiveScreen -> "Event Archive"
        Screen.WorkshopsScreen -> "Workshops"
        Screen.ProfileScreen -> "My Profile"
        Screen.SettingsScreen -> "Settings"
        Screen.DeleteAccountScreen -> "Delete Account"
        Screen.ConferenceRegistrationScreen -> "Registration"
        Screen.PaymentStatusScreen -> "Payment Status"
        Screen.MyRegistrationsScreen -> "My Registrations"
        else -> "SSP Lahore"
    }

    MainScaffold(
        currentRoute = currentRoute,
        currentTab = currentTab,
        onTabSelected = { tab ->
            when (tab) {
                BottomBarTab.HOME -> {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                }
                BottomBarTab.EVENTS -> {
                    navController.navigate(Screen.ScientificProgramsScreen.route)
                }
                BottomBarTab.FELLOWSHIP -> {
                    navController.navigate(Screen.FellowshipHubScreen.route)
                }
                BottomBarTab.MY_PORTAL -> {
                    if (user == null || user?.isGuest() == true) {
                        onParentGraph()
                    } else {
                        navController.navigate(Screen.ProfileScreen.route)
                    }
                }
                BottomBarTab.MORE -> {}
            }
        },
        onNavigate = { route ->
            val targetScreen = when (route) {
                is Route.Home -> Screen.HomeScreen
                is Route.About -> Screen.AboutScreen
                is Route.History -> Screen.HistoryScreen
                is Route.ExecutiveCouncil -> Screen.ExecutiveCouncilScreen
                is Route.Elections -> Screen.ElectionsScreen
                is Route.News -> Screen.NotificationsScreen
                is Route.Faqs -> Screen.FaqsScreen
                is Route.MemberGuide -> Screen.MemberGuideScreen
                is Route.Contact -> Screen.ContactUsScreen
                is Route.Terms -> Screen.TermsScreen
                is Route.Downloads -> Screen.DownloadsScreen
                is Route.VerifyCertificate -> Screen.VerifyCertificateScreen
                is Route.MembersDirectory -> Screen.MembersDirectoryScreen
                is Route.Events -> Screen.ScientificProgramsScreen
                is Route.EventDetail -> Screen.EventDetailScreen
                is Route.EventRegistration -> Screen.EventRegistrationScreen
                is Route.Conferences -> Screen.ConferencesScreen
                is Route.EventArchive -> Screen.EventArchiveScreen
                is Route.PaymentStatus -> Screen.PaymentStatusScreen
                is Route.Workshops -> Screen.WorkshopsScreen
                is Route.MyRegistrations -> Screen.MyRegistrationsScreen
                is Route.Profile -> Screen.ProfileScreen
                is Route.Settings -> Screen.SettingsScreen
                is Route.DeleteAccount -> Screen.DeleteAccountScreen
                is Route.FellowshipHub -> Screen.FellowshipHubScreen
                else -> Screen.HomeScreen
            }
            navController.navigate(targetScreen.route)
        },
        onNavigateBack = { navController.navigateUp() },
        canNavigateBack = canNavigateBack,
        title = screenTitle,
        userSession = userSession,
        onSignIn = onParentGraph,
        onLogout = {
            onParentGraph()
        },
        unreadNotificationCount = unreadNotifications,
        onNotificationsClick = {
            navController.navigate(Screen.NotificationsScreen.route)
        }
    ) { contentModifier ->
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = contentModifier.fillMaxSize()
        ) {
            (otherScreens() + navItems()).forEach { navItem ->
                when (navItem.screen) {
                    is Screen.ProfileScreen -> {
                        composable(route = navItem.screen.route) {
                            ProfileScreen(navController, onParentGraph = onParentGraph)
                        }
                    }
                    is Screen.DeleteAccountScreen -> {
                        composable(route = navItem.screen.route) {
                            DeleteAccountScreen(navController, onParentGraph = onParentGraph)
                        }
                    }
                    else -> {
                        composable(route = navItem.screen.route) {
                            navItem.component.invoke(navController)
                        }
                    }
                }
            }
        }
    }
}
