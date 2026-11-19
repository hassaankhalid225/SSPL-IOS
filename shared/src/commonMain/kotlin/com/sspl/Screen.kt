package com.sspl


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.sspl.resources.Res
import com.sspl.resources.about_us
import com.sspl.resources.create_post
import com.sspl.resources.delete_account
import com.sspl.resources.edit_profile
import com.sspl.resources.feedback
import com.sspl.resources.forgot_password
import com.sspl.resources.forum
import com.sspl.resources.home
import com.sspl.resources.ic_forum
import com.sspl.resources.ic_user
import com.sspl.resources.log_in
import com.sspl.resources.notifications
import com.sspl.resources.exhibition
import com.sspl.resources.exhibition_details
import com.sspl.resources.profile
import com.sspl.resources.program_detail
import com.sspl.resources.scientific_programs
import com.sspl.resources.session
import com.sspl.resources.session_detail
import com.sspl.resources.settings
import com.sspl.resources.sign_up
import com.sspl.resources.user_details
import com.sspl.resources.verify_otp
import com.sspl.resources.workshop
import com.sspl.resources.workshops
import com.sspl.ui.SplashScreen
import com.sspl.ui.about.AboutScreen
import com.sspl.ui.certificate.VerifyCertificateScreen
import com.sspl.ui.contact.ContactUsScreen
import com.sspl.ui.council.ExecutiveCouncilScreen
import com.sspl.ui.downloads.DownloadsScreen
import com.sspl.ui.elections.ElectionsScreen
import com.sspl.ui.faqs.FaqsScreen
import com.sspl.ui.guide.MemberGuideScreen
import com.sspl.ui.history.HistoryScreen
import com.sspl.ui.members.MembersDirectoryScreen
import com.sspl.ui.terms.TermsScreen
import com.sspl.ui.conference.programs.ProgramDetailScreen
import com.sspl.ui.conference.programs.ScientificProgramsScreen
import com.sspl.ui.conference.programs.SessionDetailScreen
import com.sspl.ui.conference.EventsListScreen
import com.sspl.ui.conference.EventDetailScreen
import com.sspl.ui.conference.EventRegistrationScreen
import com.sspl.ui.conference.ConferencesScreen
import com.sspl.ui.conference.EventArchiveScreen
import com.sspl.ui.conference.registration.ConferenceRegistrationScreen
import com.sspl.ui.conference.registration.PaymentStatusScreen
import com.sspl.ui.conference.registration.MyRegistrationsScreen
import com.sspl.ui.conference.workshops.WorkshopDetailScreen
import com.sspl.ui.conference.workshops.WorkshopsScreenV2
import com.sspl.ui.feedback.FeedbackScreen
import com.sspl.ui.homescreen.HomeScreen
import com.sspl.ui.kyc.ForgotPasswordScreen
import com.sspl.ui.kyc.LoginScreen
import com.sspl.ui.kyc.SignUpScreen
import com.sspl.ui.notifications.NotificationsScreen
import com.sspl.ui.otp.OtpScreen
import com.sspl.ui.session.SessionScreen
import com.sspl.ui.settings.SettingsScreen
import com.sspl.ui.settings.deleteaccount.DeleteAccountScreen
import com.sspl.ui.userdetails.EditProfileScreen
import com.sspl.ui.userdetails.ProfileScreen
import com.sspl.ui.userdetails.UserDetailsScreen
import com.sspl.ui.portal.PortalOverviewScreen
import com.sspl.ui.portal.MembershipScreen
import com.sspl.ui.portal.MembershipPlansScreen
import com.sspl.ui.portal.EventPortalScreen
import com.sspl.ui.portal.AbstractSubmissionScreen
import com.sspl.ui.portal.MySubmissionsScreen
import com.sspl.ui.portal.PresentationsScreen
import com.sspl.ui.portal.MyCertificatesScreen
import com.sspl.ui.fellowship.FellowshipHubScreen
import com.sspl.ui.fellowship.TrainingCentersScreen
import com.sspl.ui.fellowship.FellowshipGuidelinesScreen
import com.sspl.ui.fellowship.FellowApplicationScreen
import com.sspl.ui.fellowship.CenterApplicationScreen
import com.sspl.ui.fellowship.FellowDashboardScreen
import com.sspl.ui.fellowship.TrainerDashboardScreen
import com.sspl.ui.fellowship.FellowshipFeedbackScreen
import com.sspl.utils.IconResource
import org.jetbrains.compose.resources.StringResource


/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */
data class NavItem(
    val screen: Screen, val component: @Composable (navController: NavHostController) -> Unit
)

sealed class Screen(
    val route: String, val title: StringResource? = null, val icon: IconResource? = null
) {
    data object None : Screen(
        route = "none",
    )

    data object SplashScreen : Screen(route = "SplashScreen")
    data object MainScreen : Screen(route = "MainScreen")

    data object HomeScreen : Screen(
        title = Res.string.home,
        route = "HomeScreen",
        icon = IconResource.fromImageVector(imageVector = Icons.Default.Home)
    )


    data object ProfileScreen : Screen(
        title = Res.string.profile,
        route = "ProfileScreen",
        icon = IconResource.fromDrawableResource(drawable = Res.drawable.ic_user)
    )

    data object ScientificProgramsScreen : Screen(
        title = Res.string.scientific_programs,
        route = "ScientificPrograms",
    )

    data object WorkshopsScreen : Screen(
        title = Res.string.workshops,
        route = "WorkshopsScreen",
    )

    data object WorkshopDetailScreen : Screen(
        title = Res.string.workshop,
        route = "WorkshopDetailScreen",
    )


    data object ScientificProgramDetailScreen : Screen(
        title = Res.string.program_detail,
        route = "ScientificProgramDetailScreen",
    )

    data object SessionDetailScreen : Screen(
        title = Res.string.session_detail,
        route = "SessionDetailScreen",
    )


    data object SignInScreen : Screen(
        title = Res.string.log_in,
        route = "SignInScreen",
    )


    data object GetLocation : Screen(
        title = Res.string.log_in,
        route = "Location",
    )

    data object UserDetailsScreen : Screen(
        title = Res.string.user_details,
        route = "UserDetailsScreen",
    )

    data object SignUpScreen : Screen(
        title = Res.string.sign_up,
        route = "SignUpScreen",
    )

    data object ForgotPasswordScreen : Screen(
        title = Res.string.forgot_password,
        route = "ForgotPassword",
    )

    data object EditProfile : Screen(
        title = Res.string.edit_profile,
        route = "EditProfileScreen",
    )

    data object SessionScreen : Screen(
        title = Res.string.session,
        route = "SessionScreen",
    )

    data object OtpScreen : Screen(
        title = Res.string.verify_otp,
        route = "OtpScreen",
    )

    data object AboutScreen : Screen(
        title = Res.string.about_us,
        route = "AboutScreen",
    )
    data object FeedbackScreen : Screen(
        title = Res.string.feedback,
        route = "FeedbackScreen",
    )

    data object NotificationsScreen : Screen(
        title = Res.string.notifications,
        route = "NotificationsScreen",
    )

    data object SettingsScreen : Screen(
        title = Res.string.settings,
        route = "SettingsScreen",
    )

    data object DeleteAccountScreen : Screen(
        title = Res.string.delete_account,
        route = "DeleteAccountScreen",
    )

    data object ConferenceRegistrationScreen : Screen(
        route = "ConferenceRegistrationScreen",
    )

    data object PaymentStatusScreen : Screen(
        route = "PaymentStatusScreen",
    )

    data object MyRegistrationsScreen : Screen(
        route = "MyRegistrationsScreen",
    )

    data object HistoryScreen : Screen(route = "HistoryScreen")
    data object ExecutiveCouncilScreen : Screen(route = "ExecutiveCouncilScreen")
    data object ElectionsScreen : Screen(route = "ElectionsScreen")
    data object FaqsScreen : Screen(route = "FaqsScreen")
    data object MemberGuideScreen : Screen(route = "MemberGuideScreen")
    data object ContactUsScreen : Screen(route = "ContactUsScreen")
    data object TermsScreen : Screen(route = "TermsScreen")
    data object DownloadsScreen : Screen(route = "DownloadsScreen")
    data object VerifyCertificateScreen : Screen(route = "VerifyCertificateScreen")
    data object MembersDirectoryScreen : Screen(route = "MembersDirectoryScreen")
    data object FellowshipHubScreen : Screen(route = "FellowshipHubScreen")
    data object EventsListScreen : Screen(route = "EventsListScreen")
    data object EventDetailScreen : Screen(route = "EventDetailScreen")
    data object EventRegistrationScreen : Screen(route = "EventRegistrationScreen")
    data object ConferencesScreen : Screen(route = "ConferencesScreen")
    data object EventArchiveScreen : Screen(route = "EventArchiveScreen")

    // Phase 5: My Portal (D1–D9)
    data object PortalOverviewScreen : Screen(route = "PortalOverviewScreen")
    data object MembershipScreen : Screen(route = "MembershipScreen")
    data object MembershipPlansScreen : Screen(route = "MembershipPlansScreen")
    data object EventPortalScreen : Screen(route = "EventPortalScreen")
    data object AbstractSubmissionScreen : Screen(route = "AbstractSubmissionScreen")
    data object MySubmissionsScreen : Screen(route = "MySubmissionsScreen")
    data object PresentationsScreen : Screen(route = "PresentationsScreen")
    data object MyCertificatesScreen : Screen(route = "MyCertificatesScreen")

    // Phase 6: Fellowship (E1–E8)
    data object TrainingCentresScreen : Screen(route = "TrainingCentresScreen")
    data object FellowshipGuidelinesScreen : Screen(route = "FellowshipGuidelinesScreen")
    data object ApplyAsFellowScreen : Screen(route = "ApplyAsFellowScreen")
    data object ApplyAsTrainingCentreScreen : Screen(route = "ApplyAsTrainingCentreScreen")
    data object FellowDashboardScreen : Screen(route = "FellowDashboardScreen")
    data object TrainerDashboardScreen : Screen(route = "TrainerDashboardScreen")
    data object FellowshipFeedbackScreen : Screen(route = "FellowshipFeedbackScreen")
}

fun screenByRoute(route: String?): Screen? =
    (navItems() + otherScreens()).firstOrNull { it.screen.route == route }?.screen

fun navItems(): List<NavItem> =
    listOf(
        NavItem(screen = Screen.HomeScreen, component = { HomeScreen(navController = it) })
    )

fun otherScreens(): List<NavItem> = listOf(
    NavItem(
        screen = Screen.ProfileScreen,
        component = { ProfileScreen(navController = it) }),
    NavItem(
        screen = Screen.AboutScreen,
        component = { AboutScreen(navController = it) }),
    NavItem(
        screen = Screen.ScientificProgramsScreen,
        component = { ScientificProgramsScreen(navController = it) }),
    NavItem(
        screen = Screen.ScientificProgramDetailScreen,
        component = { ProgramDetailScreen(navController = it) }),
    NavItem(
        screen = Screen.SessionDetailScreen,
        component = { SessionDetailScreen(navController = it) }),
    NavItem(
        screen = Screen.SessionScreen,
        component = { SessionScreen(navController = it) }),
    NavItem(
        screen = Screen.FeedbackScreen,
        component = { FeedbackScreen(navController = it) }),
    NavItem(
        screen = Screen.UserDetailsScreen,
        component = { UserDetailsScreen(navController = it) }),
    NavItem(
        screen = Screen.EditProfile,
        component = { EditProfileScreen(navController = it) }),

    NavItem(screen = Screen.WorkshopsScreen, component = { WorkshopsScreenV2(navController = it) }),
    NavItem(
        screen = Screen.WorkshopDetailScreen,
        component = { WorkshopDetailScreen(navController = it) }),
    NavItem(
        screen = Screen.NotificationsScreen,
        component = { NotificationsScreen(navController = it) }),
    NavItem(
        screen = Screen.SettingsScreen,
        component = { SettingsScreen(navController = it) }),
    NavItem(
        screen = Screen.DeleteAccountScreen,
        component = { DeleteAccountScreen(navController = it) }),
    NavItem(
        screen = Screen.ConferenceRegistrationScreen,
        component = { ConferenceRegistrationScreen(navController = it) }),
    NavItem(
        screen = Screen.PaymentStatusScreen,
        component = { PaymentStatusScreen(navController = it) }),
    NavItem(
        screen = Screen.MyRegistrationsScreen,
        component = { 
             val confId = it.previousBackStackEntry?.savedStateHandle?.get<Long>("conferenceId") ?: 0L
             MyRegistrationsScreen(conferenceId = confId, navController = it) 
        }),
    NavItem(screen = Screen.HistoryScreen, component = { HistoryScreen(navController = it) }),
    NavItem(screen = Screen.ExecutiveCouncilScreen, component = { ExecutiveCouncilScreen(navController = it) }),
    NavItem(screen = Screen.ElectionsScreen, component = { ElectionsScreen(navController = it) }),
    NavItem(screen = Screen.FaqsScreen, component = { FaqsScreen(navController = it) }),
    NavItem(screen = Screen.MemberGuideScreen, component = { MemberGuideScreen(navController = it) }),
    NavItem(screen = Screen.ContactUsScreen, component = { ContactUsScreen(navController = it) }),
    NavItem(screen = Screen.TermsScreen, component = { TermsScreen(navController = it) }),
    NavItem(screen = Screen.DownloadsScreen, component = { DownloadsScreen(navController = it) }),
    NavItem(screen = Screen.VerifyCertificateScreen, component = { VerifyCertificateScreen(navController = it) }),
    NavItem(screen = Screen.MembersDirectoryScreen, component = { MembersDirectoryScreen(navController = it) }),
    NavItem(screen = Screen.FellowshipHubScreen, component = { FellowshipHubScreen(navController = it) }),
    NavItem(screen = Screen.EventsListScreen, component = { EventsListScreen(navController = it) }),
    NavItem(screen = Screen.EventDetailScreen, component = { EventDetailScreen(navController = it) }),
    NavItem(screen = Screen.EventRegistrationScreen, component = { EventRegistrationScreen(navController = it) }),
    NavItem(screen = Screen.ConferencesScreen, component = { ConferencesScreen(navController = it) }),
    NavItem(screen = Screen.EventArchiveScreen, component = { EventArchiveScreen(navController = it) }),

    // Phase 5: My Portal (D1–D9)
    NavItem(screen = Screen.PortalOverviewScreen, component = { PortalOverviewScreen(navController = it) }),
    NavItem(screen = Screen.MembershipScreen, component = { MembershipScreen(navController = it) }),
    NavItem(screen = Screen.MembershipPlansScreen, component = { MembershipPlansScreen(navController = it) }),
    NavItem(screen = Screen.EventPortalScreen, component = { EventPortalScreen(navController = it) }),
    NavItem(screen = Screen.AbstractSubmissionScreen, component = { AbstractSubmissionScreen(navController = it) }),
    NavItem(screen = Screen.MySubmissionsScreen, component = { MySubmissionsScreen(navController = it) }),
    NavItem(screen = Screen.PresentationsScreen, component = { PresentationsScreen(navController = it) }),
    NavItem(screen = Screen.MyCertificatesScreen, component = { MyCertificatesScreen(navController = it) }),

    // Phase 6: Fellowship (E1–E8)
    NavItem(screen = Screen.TrainingCentresScreen, component = { TrainingCentersScreen(navController = it) }),
    NavItem(screen = Screen.FellowshipGuidelinesScreen, component = { FellowshipGuidelinesScreen(navController = it) }),
    NavItem(screen = Screen.ApplyAsFellowScreen, component = { FellowApplicationScreen(navController = it) }),
    NavItem(screen = Screen.ApplyAsTrainingCentreScreen, component = { CenterApplicationScreen(navController = it) }),
    NavItem(screen = Screen.FellowDashboardScreen, component = { FellowDashboardScreen(navController = it) }),
    NavItem(screen = Screen.TrainerDashboardScreen, component = { TrainerDashboardScreen(navController = it) }),
    NavItem(screen = Screen.FellowshipFeedbackScreen, component = { FellowshipFeedbackScreen(navController = it) }),
)

fun kycScreens(): List<NavItem> = listOf(
    NavItem(screen = Screen.SplashScreen, component = { SplashScreen(navController = it) }),
    NavItem(screen = Screen.SignUpScreen, component = { SignUpScreen(navController = it) }),
    NavItem(screen = Screen.SignInScreen, component = { LoginScreen(navController = it) }),
    NavItem(screen = Screen.OtpScreen, component = { OtpScreen(navController = it) }),
    NavItem(
        screen = Screen.ForgotPasswordScreen,
        component = { ForgotPasswordScreen(navController = it) }),
    NavItem(
        screen = Screen.UserDetailsScreen,
        component = { UserDetailsScreen(navController = it) }),
)
