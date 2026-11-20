package com.sspl.ui.conference.workshops

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.sspl.ui.conference.EventScope
import com.sspl.ui.conference.EventsBrowser
import com.sspl.ui.conference.EventsListViewModel
import com.sspl.ui.conference.WorkshopsIntro
import org.koin.compose.viewmodel.koinViewModel

/**
 * C7: Pre-event workshops. Mirrors the web `/conferences/workshops` page, which lists
 * /external/events with type WORKSHOP; detail + registration go through the same
 * event detail / registration flow as every other event.
 */
@Composable
fun WorkshopsScreenV2(
    navController: NavController,
    viewModel: EventsListViewModel = koinViewModel()
) {
    EventsBrowser(
        navController = navController,
        viewModel = viewModel,
        scope = EventScope.WORKSHOPS,
        intro = WorkshopsIntro
    )
}
