package com.sspl.ui.conference.programs

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
import com.sspl.ui.userdetails.ProfileViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

private val lenientJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

/** C6: Scientific programme (legacy /conferences/{id} days → sessions) with attendance + feedback. */
@Composable
fun ProgramDetailScreen(
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
            title = "Programme unavailable",
            message = "Open the programme again from the event.",
            icon = Icons.Default.EventBusy,
            modifier = Modifier.fillMaxSize()
        )
        return
    }
    val isGuest by profileViewModel.isGuestUser.collectAsStateWithLifecycle()

    ConferenceProgrammeScreen(
        conference = conference,
        eyebrow = "SCIENTIFIC PROGRAMME",
        viewModel = viewModel,
        attendanceViewModel = attendanceViewModel,
        isGuestUser = isGuest != false,
        allowFeedback = true,
        onSessionClick = { session ->
            navController.currentBackStackEntry?.savedStateHandle?.set("data", Json.encodeToString(session))
            navController.navigate(Screen.SessionDetailScreen.route) { launchSingleTop = true }
        },
        onFeedback = { session ->
            navController.currentBackStackEntry?.savedStateHandle?.set("conferenceID", conference.id)
            navController.currentBackStackEntry?.savedStateHandle?.set("sessionID", session.id)
            navController.navigate(Screen.FeedbackScreen.route) { launchSingleTop = true }
        }
    )
}
