package com.sspl.ui.session

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.EmptyState
import com.sspl.ui.conference.programs.SessionDetailScreen

/**
 * Live hall session. Previously rendered a hard-coded dummy session ("Jetpack Compose…") to
 * real users. Now: if a session was passed (`data`, small Session JSON) show its detail;
 * otherwise explain where to pick a session from.
 */
@Composable
fun SessionScreen(navController: NavController) {
    val hasSession = remember {
        !navController.previousBackStackEntry?.savedStateHandle?.get<String>("data").isNullOrBlank()
    }
    if (hasSession) {
        SessionDetailScreen(navController = navController)
        return
    }
    EmptyState(
        title = "No live session selected",
        message = "Pick a session from your event programme, or join with the hall code shown at the venue in the Event Portal.",
        icon = SsplIcons.CoPresent,
        actionText = "Open Event Portal",
        onActionClick = {
            if (navController.previousBackStackEntry?.destination?.route == Screen.EventPortalScreen.route) {
                navController.popBackStack()
            } else {
                navController.navigate(Screen.EventPortalScreen.route) { launchSingleTop = true }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
