package com.sspl.ui.conference.programs

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.sspl.ui.conference.EventsListScreen

@Composable
fun ScientificProgramsScreen(
    navController: NavController
) {
    EventsListScreen(navController = navController)
}
