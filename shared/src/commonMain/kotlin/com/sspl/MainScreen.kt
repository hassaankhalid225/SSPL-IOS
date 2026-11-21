package com.sspl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sspl.core.repositories.NotificationRepository
import com.sspl.navigation.BottomBarTab
import com.sspl.navigation.DeepLink
import com.sspl.navigation.DeepLinkBus
import com.sspl.navigation.MainScaffold
import com.sspl.navigation.Route
import com.sspl.session.UserSession
import com.sspl.ui.settings.deleteaccount.DeleteAccountScreen
import com.sspl.ui.userdetails.ProfileScreen
import com.sspl.ui.portal.openMembershipApplication
import com.sspl.ui.portal.openMembershipPlans
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

    // Deep links (sspl://events/{id}, https://ssplahore.com/verify-certificate/{serial}, …) queued by the host.
    val pendingLink by DeepLinkBus.pending.collectAsState()
    LaunchedEffect(pendingLink, backStackEntry != null) {
        val link = pendingLink ?: return@LaunchedEffect
        if (backStackEntry == null) return@LaunchedEffect // NavHost graph not set yet
        when (link) {
            is DeepLink.EventDetail -> {
                navController.currentBackStackEntry?.savedStateHandle?.set("eventId", link.eventId)
                navController.navigate(Screen.EventDetailScreen.route)
            }
            is DeepLink.VerifyCertificate -> {
                navController.currentBackStackEntry?.savedStateHandle?.set("certSerial", link.serial)
                navController.navigate(Screen.VerifyCertificateScreen.route)
            }
        }
        DeepLinkBus.consume(link)
    }

    val currentTab = when (currentScreen) {
        Screen.HomeScreen -> BottomBarTab.HOME
        Screen.ScientificProgramsScreen, Screen.WorkshopsScreen, Screen.EventsListScreen, Screen.ConferencesScreen -> BottomBarTab.EVENTS
        Screen.FellowshipHubScreen, Screen.TrainingCentresScreen, Screen.FellowshipGuidelinesScreen,
        Screen.ApplyAsFellowScreen, Screen.ApplyAsTrainingCentreScreen, Screen.FellowDashboardScreen,
        Screen.TrainerDashboardScreen, Screen.FellowshipFeedbackScreen -> BottomBarTab.FELLOWSHIP
        Screen.PortalOverviewScreen, Screen.MembershipScreen, Screen.MembershipPlansScreen, Screen.MembershipApplicationScreen,
        Screen.MyRegistrationsScreen, Screen.EventPortalScreen, Screen.AbstractSubmissionScreen,
        Screen.MySubmissionsScreen, Screen.PresentationsScreen, Screen.MyCertificatesScreen,
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
        Screen.PortalOverviewScreen -> Route.PortalOverview
        Screen.MembershipScreen -> Route.Membership
        Screen.MembershipPlansScreen -> Route.MembershipPlans
        Screen.MembershipApplicationScreen -> Route.MembershipApplication()
        Screen.EventPortalScreen -> Route.EventPortal()
        Screen.AbstractSubmissionScreen -> Route.SubmitAbstract()
        Screen.MySubmissionsScreen -> Route.MySubmissions
        Screen.PresentationsScreen -> Route.Presentations
        Screen.MyCertificatesScreen -> Route.MyCertificates
        Screen.FellowshipHubScreen -> Route.FellowshipHub
        Screen.TrainingCentresScreen -> Route.TrainingCentres
        Screen.FellowshipGuidelinesScreen -> Route.FellowshipGuidelines
        Screen.ApplyAsFellowScreen -> Route.ApplyAsFellow
        Screen.ApplyAsTrainingCentreScreen -> Route.ApplyAsTrainingCentre
        Screen.FellowDashboardScreen -> Route.FellowDashboard
        Screen.TrainerDashboardScreen -> Route.TrainerDashboard
        Screen.FellowshipFeedbackScreen -> Route.FellowshipFeedback
        Screen.EditProfile, Screen.UserDetailsScreen -> Route.Profile
        Screen.PaymentStatusScreen, Screen.ConferenceRegistrationScreen,
        Screen.WorkshopDetailScreen, Screen.ScientificProgramDetailScreen,
        Screen.SessionDetailScreen, Screen.SessionScreen, Screen.FeedbackScreen -> Route.Events
        else -> Route.Home
    }

    val screenTitle = when (currentScreen) {
        Screen.HomeScreen -> "SSPL Society"
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
        Screen.EditProfile -> "Edit profile"
        Screen.UserDetailsScreen -> "Complete profile"
        Screen.WorkshopDetailScreen -> "Workshop"
        Screen.ScientificProgramDetailScreen -> "Programme"
        Screen.SessionDetailScreen -> "Session"
        Screen.SessionScreen -> "Live session"
        Screen.FeedbackScreen -> "Feedback"
        Screen.ConferenceRegistrationScreen -> "Registration"
        Screen.PaymentStatusScreen -> "Payment Status"
        Screen.MyRegistrationsScreen -> "My Registrations"
        Screen.PortalOverviewScreen -> "Member Desk"
        Screen.MembershipScreen -> "Membership"
        Screen.MembershipPlansScreen -> "Membership Plans"
        Screen.MembershipApplicationScreen -> "Membership Application"
        Screen.EventPortalScreen -> "Event Portal"
        Screen.AbstractSubmissionScreen -> "Submit Abstract"
        Screen.MySubmissionsScreen -> "My Submissions"
        Screen.PresentationsScreen -> "Presentations"
        Screen.MyCertificatesScreen -> "My Certificates"
        Screen.FellowshipHubScreen -> "Fellowship Hub"
        Screen.TrainingCentresScreen -> "Training Centres"
        Screen.FellowshipGuidelinesScreen -> "Fellowship Guidelines"
        Screen.ApplyAsFellowScreen -> "Apply as Fellow"
        Screen.ApplyAsTrainingCentreScreen -> "Apply as Training Centre"
        Screen.FellowDashboardScreen -> "Fellow Logbook"
        Screen.TrainerDashboardScreen -> "Trainer Desk"
        Screen.FellowshipFeedbackScreen -> "Fellowship Feedback"
        else -> "SSPL Society"
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
                    navController.navigate(Screen.ScientificProgramsScreen.route) {
                        popUpTo(Screen.HomeScreen.route)
                        launchSingleTop = true
                    }
                }
                BottomBarTab.FELLOWSHIP -> {
                    navController.navigate(Screen.FellowshipHubScreen.route) {
                        popUpTo(Screen.HomeScreen.route)
                        launchSingleTop = true
                    }
                }
                BottomBarTab.MY_PORTAL -> {
                    if (user == null || user?.isGuest() == true) {
                        onParentGraph()
                    } else {
                        navController.navigate(Screen.PortalOverviewScreen.route) {
                            popUpTo(Screen.HomeScreen.route)
                            launchSingleTop = true
                        }
                    }
                }
                BottomBarTab.MORE -> {}
            }
        },
        onNavigate = { route ->
            if (route is Route.MembershipApplication) {
                // Web: no selected plan → /membership-plans.
                val planId = route.planId
                if (planId != null) navController.openMembershipApplication(planId, isAlreadyMember = false)
                else navController.openMembershipPlans(isAlreadyMember = false)
                return@MainScaffold
            }
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
                is Route.PortalOverview -> Screen.PortalOverviewScreen
                is Route.Membership -> Screen.MembershipScreen
                is Route.MembershipPlans -> Screen.MembershipPlansScreen
                is Route.EventPortal -> Screen.EventPortalScreen
                is Route.SubmitAbstract -> Screen.AbstractSubmissionScreen
                is Route.MySubmissions -> Screen.MySubmissionsScreen
                is Route.Presentations -> Screen.PresentationsScreen
                is Route.MyCertificates -> Screen.MyCertificatesScreen
                is Route.TrainingCentres -> Screen.TrainingCentresScreen
                is Route.FellowshipGuidelines -> Screen.FellowshipGuidelinesScreen
                is Route.ApplyAsFellow -> Screen.ApplyAsFellowScreen
                is Route.ApplyAsTrainingCentre -> Screen.ApplyAsTrainingCentreScreen
                is Route.FellowDashboard -> Screen.FellowDashboardScreen
                is Route.TrainerDashboard -> Screen.TrainerDashboardScreen
                is Route.FellowshipFeedback -> Screen.FellowshipFeedbackScreen
                else -> Screen.HomeScreen
            }
            navController.navigate(targetScreen.route) { launchSingleTop = true }
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
        isHomeScreen = currentScreen == Screen.HomeScreen,
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
                    is Screen.SettingsScreen -> {
                        composable(route = navItem.screen.route) {
                            com.sspl.ui.settings.SettingsScreen(navController, onParentGraph = onParentGraph)
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
