package com.sspl.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    // --- A. Auth & Account ---
    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object SignUp : Route

    @Serializable
    data class Otp(val phone: String, val flow: String = "signup") : Route

    @Serializable
    data object ForgotPassword : Route

    @Serializable
    data class ResetPassword(val token: String) : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object EditProfile : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object DeleteAccount : Route

    // --- B. Home & Public Info ---
    @Serializable
    data object Home : Route

    @Serializable
    data object About : Route

    @Serializable
    data object History : Route

    @Serializable
    data object ExecutiveCouncil : Route

    @Serializable
    data object Elections : Route

    @Serializable
    data class ElectionDetail(val id: Long) : Route

    @Serializable
    data object News : Route

    @Serializable
    data object Faqs : Route

    @Serializable
    data object MemberGuide : Route

    @Serializable
    data object Contact : Route

    @Serializable
    data object Terms : Route

    @Serializable
    data object Downloads : Route

    @Serializable
    data class VerifyCertificate(val serial: String? = null) : Route

    @Serializable
    data object MembersDirectory : Route

    // --- C. Events & Conferences ---
    @Serializable
    data object Events : Route

    @Serializable
    data class EventDetail(val id: Long) : Route

    @Serializable
    data class EventRegistration(val id: Long) : Route

    @Serializable
    data object Conferences : Route

    @Serializable
    data class ConferenceDetail(val id: Long) : Route

    @Serializable
    data class ConferenceRegistration(val id: Long) : Route

    @Serializable
    data class ScientificProgramme(val conferenceId: Long) : Route

    @Serializable
    data class SessionDetail(val sessionId: Long) : Route

    @Serializable
    data class Workshops(val conferenceId: Long? = null) : Route

    @Serializable
    data class WorkshopDetail(val workshopId: Long) : Route

    @Serializable
    data object EventArchive : Route

    @Serializable
    data class PaymentStatus(val registrationId: Long = 0L, val isSuccess: Boolean = false) : Route

    // --- D. My Portal (Member Workspace) ---
    @Serializable
    data object PortalOverview : Route

    @Serializable
    data object Membership : Route

    @Serializable
    data object MembershipPlans : Route

    @Serializable
    data class MembershipApplication(val planId: Long? = null) : Route

    @Serializable
    data object MyRegistrations : Route

    @Serializable
    data class EventPortal(val conferenceId: Long? = null) : Route

    @Serializable
    data class SubmitAbstract(val eventId: Long? = null) : Route

    @Serializable
    data object MySubmissions : Route

    @Serializable
    data object Presentations : Route

    @Serializable
    data object MyCertificates : Route

    // --- E. Fellowship ---
    @Serializable
    data object FellowshipHub : Route

    @Serializable
    data object TrainingCentres : Route

    @Serializable
    data object FellowshipGuidelines : Route

    @Serializable
    data object ApplyAsFellow : Route

    @Serializable
    data object ApplyAsTrainingCentre : Route

    @Serializable
    data object FellowDashboard : Route

    @Serializable
    data object TrainerDashboard : Route

    @Serializable
    data object FellowshipFeedback : Route
}

enum class BottomBarTab(val label: String) {
    HOME("Home"),
    EVENTS("Events"),
    FELLOWSHIP("Fellowship"),
    MY_PORTAL("My Portal"),
    MORE("More")
}

