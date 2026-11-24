package com.sspl.ui.conference.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.models.Session
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ExpandableText
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.icons.SsplIcons
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.SessionBreakDown
import com.sspl.ui.conference.AttendanceViewModel
import com.sspl.ui.conference.components.AddOnSetsList
import com.sspl.ui.conference.components.safeTime
import com.sspl.ui.portal.portalViewModel
import com.sspl.utils.DisplayDates
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** C6: Session detail (scientific programme). Receives the Session JSON as `data`. */
@Composable
fun SessionDetailScreen(
    navController: NavController,
    attendanceViewModel: AttendanceViewModel = koinViewModel(),
    urlOpener: UrlOpener = koinInject()
) {
    val handle = remember { navController.previousBackStackEntry?.savedStateHandle }
    val session: Session? = remember {
        handle?.get<String>("data")?.let {
            runCatching { Json { ignoreUnknownKeys = true; coerceInputValues = true }.decodeFromString<Session>(it) }.getOrNull()
        }
    }
    val conferenceId = remember { handle?.get<Long>("conferenceId") ?: 0L }
    val isRegistered = remember { handle?.get<Boolean>("isRegistered") ?: true }

    if (session == null) {
        EmptyState(
            title = "Session unavailable",
            message = "Open the session again from the event schedule.",
            icon = SsplIcons.EventBusy,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val shell = LocalShellActions.current
    val snackbar = remember { SnackbarHostState() }
    val actionsViewModel: SessionActionsViewModel = portalViewModel { SessionActionsViewModel(it.get(), it.get(), it.get()) }

    val showLocationSuccess by attendanceViewModel.showAttendanceDialogByLocation.collectAsStateWithLifecycle()
    val showImageSuccess by attendanceViewModel.showAttendanceDialogByImage.collectAsStateWithLifecycle()

    if (showLocationSuccess) {
        AttendanceDialog(title = "Attendance marked", description = "Your attendance has been marked.", onDismiss = attendanceViewModel::onDismissDialogs)
    }
    if (showImageSuccess) {
        AttendanceDialog(onDismiss = attendanceViewModel::onDismissDialogs)
    }

    val effectiveConfId = remember(conferenceId, session.id) { if (conferenceId > 0) conferenceId else session.id }

    val feedbackSessionId = navController.currentBackStackEntry?.savedStateHandle?.get<Long>("feedbackSubmittedSessionId")
    LaunchedEffect(feedbackSessionId) {
        if (feedbackSessionId != null && feedbackSessionId == session.id) {
            actionsViewModel.onFeedbackSubmitted(session.id)
            if (effectiveConfId > 0) actionsViewModel.load(effectiveConfId)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Long>("feedbackSubmittedSessionId")
        }
    }

    LaunchedEffect(effectiveConfId, shell.isGuest) {
        if (!shell.isGuest && effectiveConfId > 0) actionsViewModel.load(effectiveConfId)
    }

    LaunchedEffect(Unit) {
        attendanceViewModel.attendanceViaLocationStatus.collect { status ->
            when (status) {
                is ApiStates.Failure -> status.error.message.takeIf { it.isNotBlank() }?.let { snackbar.showSnackbar(it) }
                ApiStates.Loading -> snackbar.showSnackbar("Marking attendance…")
                is ApiStates.Success -> {
                    actionsViewModel.onAttendanceMarked(session.id, status.data?.status)
                    if (effectiveConfId > 0) {
                        actionsViewModel.load(effectiveConfId)
                    }
                    snackbar.showSnackbar("Attendance marked successfully")
                }
                ApiStates.Idle -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        attendanceViewModel.attendanceViaImageStatus.collect { status ->
            when (status) {
                is ApiStates.Failure -> status.error.message.takeIf { it.isNotBlank() }?.let { snackbar.showSnackbar(it) }
                ApiStates.Loading -> snackbar.showSnackbar("Uploading selfie…")
                is ApiStates.Success -> {
                    actionsViewModel.onAttendanceMarked(session.id, status.data?.status ?: "PENDING")
                    if (effectiveConfId > 0) {
                        actionsViewModel.load(effectiveConfId)
                    }
                    snackbar.showSnackbar("Selfie uploaded for attendance approval")
                }
                ApiStates.Idle -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        actionsViewModel.events.collect { ev ->
            when (ev) {
                is SessionActionsEvent.OpenUrl -> urlOpener.openUrl(ev.url)
                is SessionActionsEvent.Message -> snackbar.showSnackbar(ev.text)
            }
        }
    }

    var now by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = Clock.System.now().toEpochMilliseconds()
        }
    }

    val actions by actionsViewModel.state.collectAsStateWithLifecycle()
    val ui = actionsViewModel.uiFor(actions, session, now)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                GradientHeroCard {
                    GlassPill(if (session.isBreak) "BREAK" else "SESSION")
                    Spacer(Modifier.height(10.dp))
                    Text(session.title.trim(), style = AppTheme.typography.headlineSmall.copy(color = Color.White))
                }
            }

            // Attendance · Feedback · Certificate Flow
            if (!session.isBreak && !shell.isGuest) {
                item(key = "actions") {
                    SsplCard {
                        Text("SESSION ACTIONS", style = AppTheme.typography.overline)
                        Spacer(Modifier.height(8.dp))
                        SessionActions(
                            session = session,
                            ui = ui,
                            isRegistered = isRegistered,
                            needsPayment = actions.needsPayment,
                            allowFeedback = true,
                            certificateLoading = actions.certificateLoadingId == session.id,
                            showCertificate = true,
                            snackbar = snackbar,
                            onFeedback = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("conferenceID", effectiveConfId)
                                navController.currentBackStackEntry?.savedStateHandle?.set("sessionID", session.id)
                                navController.navigate(Screen.FeedbackScreen.route) { launchSingleTop = true }
                            },
                            onMarkAttendance = { lat, lng ->
                                attendanceViewModel.markAttendanceViaLocation(conferenceId = effectiveConfId, sessionId = session.id, lat = lat, lng = lng)
                            },
                            onMarkAttendanceByImage = { img ->
                                attendanceViewModel.markAttendanceViaImage(conferenceId = effectiveConfId, sessionId = session.id, image = img)
                            },
                            onCertificate = {
                                actionsViewModel.getCertificate(effectiveConfId, session.id)
                            }
                        )
                    }
                }
            }

            item(key = "facts") {
                SsplCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DisplayDates.medium(session.startsAt).takeIf { it.isNotBlank() }?.let {
                            FactRow(SsplIcons.CalendarMonth, "Date", "${DisplayDates.weekdayShort(session.startsAt)}, $it")
                        }
                        val time = listOf(safeTime(session.startsAt), safeTime(session.endsAt)).filter { it.isNotBlank() }.joinToString(" – ")
                        if (time.isNotBlank()) FactRow(SsplIcons.Schedule, "Time", time)
                        val venue = listOfNotNull(session.venue?.title ?: session.venue?.name, session.venue?.address)
                            .filter { it.isNotBlank() }.joinToString(", ")
                        if (venue.isNotBlank()) FactRow(SsplIcons.Place, "Venue", venue)
                    }
                }
            }
            session.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                item(key = "summary") {
                    SsplCard {
                        Text("OVERVIEW", style = AppTheme.typography.overline)
                        Spacer(Modifier.height(6.dp))
                        ExpandableText(summary.trim(), maxLines = 6)
                    }
                }
            }
            session.breakdown?.takeIf { it.isNotEmpty() }?.let { breakdown ->
                item(key = "breakdown") {
                    SsplCard {
                        Text("RUNDOWN & SPEAKERS", style = AppTheme.typography.overline)
                        Spacer(Modifier.height(6.dp))
                        SessionBreakDown(breakdown = breakdown, isClickEnabled = false)
                    }
                }
            }
            session.addOnSets?.takeIf { it.isNotEmpty() }?.let { sets ->
                item(key = "addons") {
                    Column {
                        Text("ADD-ONS", style = AppTheme.typography.overline, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                        AddOnSetsList(sets)
                    }
                }
            }
            session.webLink?.takeIf { it.isNotBlank() }?.let { link ->
                item(key = "link") {
                    OutlineButton("Open session link", onClick = { urlOpener.openUrl(link) }, icon = SsplIcons.OpenInNew)
                }
            }
        }
    }
}
