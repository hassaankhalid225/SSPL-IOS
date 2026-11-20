package com.sspl.ui.conference

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel

/** C4: Annual conferences = /external/events filtered to PROGRAM + CONFERENCE (same as web `/conferences`). */
@Composable
fun ConferencesScreen(
    navController: NavController,
    viewModel: EventsListViewModel = koinViewModel()
) {
    EventsBrowser(
        navController = navController,
        viewModel = viewModel,
        scope = EventScope.CONFERENCES,
        intro = ConferencesIntro
    )
}
