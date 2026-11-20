package com.sspl.ui.conference.workshops

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.Conference
import com.sspl.designsystem.components.EmptyState
import com.sspl.ui.conference.AttendanceViewModel
import com.sspl.ui.conference.ConferenceDetailViewModel
import com.sspl.ui.conference.programs.ConferenceProgrammeScreen
import com.sspl.ui.userdetails.ProfileViewModel
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

private val lenientJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

/**
 * Legacy workshop detail (GET /conferences/{id}) with sessions + attendance. Kept for
 * routes that still pass a `data` Conference; the Workshops list now opens EventDetail.
 * Feedback stays hidden here exactly as before.
 */
@Composable
fun WorkshopDetailScreen(
    navController: NavController,
    viewModel: ConferenceDetailViewModel = koinViewModel(),
    attendanceViewModel: AttendanceViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val conference = remember {
        navController.previousBackStackEntry?.savedStateHandle?.get<String>("data")
            ?.let { runCatching { lenientJson.decodeFromString<Conference>(it) }.getOrNull() }
    }
    if (conference == null) {
        EmptyState(
            title = "Workshop unavailable",
            message = "Open the workshop again from the workshops list.",
            icon = Icons.Default.EventBusy,
            modifier = Modifier.fillMaxSize()
        )
        return
    }
    val isGuest by profileViewModel.isGuestUser.collectAsStateWithLifecycle()

    ConferenceProgrammeScreen(
        conference = conference,
        eyebrow = "WORKSHOP",
        viewModel = viewModel,
        attendanceViewModel = attendanceViewModel,
        isGuestUser = isGuest != false,
        allowFeedback = false,
        onSessionClick = null,
        onFeedback = { session ->
            navController.currentBackStackEntry?.savedStateHandle?.set("conferenceID", conference.id)
            navController.currentBackStackEntry?.savedStateHandle?.set("sessionID", session.id)
            navController.navigate(Screen.FeedbackScreen.route) { launchSingleTop = true }
        }
    )
}
