package com.sspl.ui.conference

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SkeletonVariant
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.conference.components.EventMediaRow
import com.sspl.ui.conference.components.FeaturedEventCard
import org.koin.compose.viewmodel.koinViewModel

/** Events tab (C1): all /external/events, filtered locally by status, category and search. */
@Composable
fun EventsListScreen(
    navController: NavController,
    viewModel: EventsListViewModel = koinViewModel()
) {
    EventsBrowser(navController = navController, viewModel = viewModel, scope = EventScope.ALL)
}

/** Navigate to event detail passing ONLY the id (never the event JSON: it embeds multi-MB base64). */
internal fun NavController.openEventDetail(eventId: Long) {
    currentBackStackEntry?.savedStateHandle?.set("eventId", eventId)
    navigate(Screen.EventDetailScreen.route) { launchSingleTop = true }
}

internal data class EventsIntro(
    val eyebrow: String,
    val title: String,
    val summary: String,
    val icon: ImageVector
)

/**
 * Shared list body for Events / Conferences / Workshops.
 *
 * Scroll stability:
 *  - one LazyColumn, no nested vertical scroll containers;
 *  - stable keys (event id) on every item; featured card keyed separately;
 *  - fixed aspect ratios on all images (no height change when the bitmap arrives);
 *  - when filters change and the user had scrolled into the list, we scroll back to the
 *    tabs header explicitly instead of letting key-anchoring jump to wherever the
 *    previous first-visible event moved to.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EventsBrowser(
    navController: NavController,
    viewModel: EventsListViewModel,
    scope: EventScope,
    intro: EventsIntro? = null
) {
    LaunchedEffect(scope) { viewModel.setScope(scope) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val tabsIndex = if (intro != null) 2 else 1

    LaunchedEffect(state.tab, state.category) {
        if (listState.firstVisibleItemIndex > tabsIndex) listState.scrollToItem(tabsIndex)
    }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            if (intro != null) {
                item(key = "intro", contentType = "intro") {
                    IntroHero(intro, state)
                }
            }
            item(key = "search", contentType = "search") {
              Column {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                    SearchBar(
                        query = state.query,
                        onQueryChange = viewModel::setSearchQuery,
                        placeholder = "Search by title, venue or type",
                        onClear = { viewModel.setSearchQuery("") }
                    )
                    if (scope == EventScope.ALL) {
                        Spacer(Modifier.height(10.dp))
                    }
                }
                if (scope == EventScope.ALL) {
                    ChipRow(
                        options = EventCategory.entries.map { it.label },
                        selected = state.category.label,
                        onSelect = { label -> EventCategory.entries.firstOrNull { it.label == label }?.let(viewModel::setCategory) }
                    )
                }
              }
            }
            stickyHeader(key = "tabs", contentType = "tabs") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.background)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    SegmentedTabs(
                        options = EventStatusTab.entries.map { it.label },
                        selectedIndex = state.tab.ordinal,
                        onSelect = { viewModel.setStatusTab(EventStatusTab.entries[it]) },
                        counts = if (state.hasData) state.counts else null
                    )
                }
            }

            when {
                state.isLoading && !state.hasData -> item(key = "skeleton", contentType = "skeleton") {
                    SkeletonList(count = 3, variant = SkeletonVariant.MEDIA)
                }

                state.errorMessage != null && !state.hasData -> item(key = "error", contentType = "state") {
                    ErrorState(
                        title = "Events didn't load",
                        message = state.errorMessage ?: "",
                        onRetry = viewModel::retry,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }

                state.visible.isEmpty() -> item(key = "empty", contentType = "state") {
                    val filtered = state.query.isNotBlank() || state.category != EventCategory.ALL
                    EmptyState(
                        title = when {
                            filtered -> "No matching events"
                            state.tab == EventStatusTab.ONGOING -> "Nothing happening right now"
                            state.tab == EventStatusTab.PAST -> "No past events yet"
                            else -> "No upcoming events yet"
                        },
                        message = if (filtered) "Try a different search or category."
                        else "New CME events, workshops and meetings are announced here first.",
                        icon = if (filtered) SsplIcons.SearchOff else SsplIcons.CalendarMonth,
                        actionText = if (filtered) "Clear filters" else if (state.tab != EventStatusTab.PAST) "See past events" else null,
                        onActionClick = {
                            if (filtered) viewModel.clearFilters() else viewModel.setStatusTab(EventStatusTab.PAST)
                        },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }

                else -> {
                    if (state.errorMessage != null) {
                        item(key = "staleBanner", contentType = "banner") {
                            ListRowCard(
                                title = "Showing saved events",
                                subtitle = "We couldn't refresh just now. Pull down to try again.",
                                leading = {
                                    IconTile(
                                        SsplIcons.WifiOff,
                                        tint = AppTheme.colors.warning,
                                        background = AppTheme.colors.warningContainer
                                    )
                                },
                                onClick = viewModel::retry,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                    val first = state.visible.first()
                    item(key = "featured_${first.id}", contentType = "featured") {
                        FeaturedEventCard(
                            event = first,
                            onClick = { navController.openEventDetail(first.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    val rest = state.visible.drop(1)
                    if (rest.isNotEmpty()) {
                        item(key = "moreHeader", contentType = "header") {
                            Text(
                                text = "More ${state.tab.label.lowercase()} · ${rest.size}",
                                style = AppTheme.typography.overline,
                                modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
                            )
                        }
                    }
                    items(rest, key = { "event_${it.id}" }, contentType = { "row" }) { e ->
                        EventMediaRow(
                            event = e,
                            onClick = { navController.openEventDetail(e.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            item(key = "archive", contentType = "footer") {
                ListRowCard(
                    title = "Event archive",
                    subtitle = "Slides, posters and abstract books from past meetings",
                    leading = { IconTile(SsplIcons.History) },
                    onClick = { navController.navigate(Screen.EventArchiveScreen.route) { launchSingleTop = true } },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun IntroHero(intro: EventsIntro, state: EventsUiState) {
    GradientHeroCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
        GlassPill(intro.eyebrow, icon = intro.icon)
        Spacer(Modifier.height(10.dp))
        Text(intro.title, style = AppTheme.typography.headlineSmall.copy(color = Color.White))
        Spacer(Modifier.height(4.dp))
        Text(intro.summary, style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.78f)))
        if (state.hasData) {
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                EventStatusTab.entries.forEachIndexed { i, t ->
                    HeroStat("${state.counts.getOrElse(i) { 0 }}", t.label, Modifier.weight(1f))
                }
            }
        }
    }
}

internal val ConferencesIntro = EventsIntro(
    eyebrow = "ANNUAL EVENTS",
    title = "Conferences & scientific programmes",
    summary = "Register here, then use the Event Portal for sessions, attendance, feedback and certificates.",
    icon = SsplIcons.Event
)

internal val WorkshopsIntro = EventsIntro(
    eyebrow = "PRE-EVENT WORKSHOPS",
    title = "Hands-on workshops",
    summary = "Workshops have their own registration. Attendance and certificates follow the member portal flow.",
    icon = SsplIcons.Event
)
