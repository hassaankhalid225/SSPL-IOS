package com.sspl.ui.conference.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.models.Session
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.ExpandableText
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.MarkAttendanceButton
import com.sspl.ui.conference.AttendanceViewModel
import com.sspl.ui.conference.ConferenceDetailViewModel
import com.sspl.ui.conference.components.AddOnSetsList
import com.sspl.ui.conference.components.SessionScheduleRow
import com.sspl.utils.DisplayDates
import com.sspl.utils.isInRange
import com.sspl.utils.isNowAfter
import dev.jordond.compass.Coordinates

/**
 * Shared body for the legacy conference programme / workshop detail (GET /conferences/{id}?days).
 * Attendance (GPS → selfie fallback) and feedback logic is unchanged; only the layout is new.
 */
@Composable
internal fun ConferenceProgrammeScreen(
    conference: Conference,
    eyebrow: String,
    viewModel: ConferenceDetailViewModel,
    attendanceViewModel: AttendanceViewModel,
    isGuestUser: Boolean,
    allowFeedback: Boolean,
    onSessionClick: ((Session) -> Unit)?,
    onFeedback: (Session) -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val showLocationSuccess by attendanceViewModel.showAttendanceDialogByLocation.collectAsStateWithLifecycle()
    val showImageSuccess by attendanceViewModel.showAttendanceDialogByImage.collectAsStateWithLifecycle()

    if (showLocationSuccess) {
        AttendanceDialog(title = "Attendance marked", description = "Your attendance has been marked.", onDismiss = attendanceViewModel::onDismissDialogs)
    }
    if (showImageSuccess) {
        AttendanceDialog(onDismiss = attendanceViewModel::onDismissDialogs)
    }

    LaunchedEffect(conference.id) { viewModel.getConferenceById(conference.id) }
    LaunchedEffect(Unit) {
        attendanceViewModel.attendanceViaLocationStatus.collect { status ->
            when (status) {
                is ApiStates.Failure -> status.error.message?.let { snackbar.showSnackbar(it) }
                ApiStates.Loading -> snackbar.showSnackbar("Marking attendance…")
                is ApiStates.Success -> snackbar.showSnackbar("Attendance marked successfully")
                ApiStates.Idle -> Unit
            }
        }
    }
    LaunchedEffect(Unit) {
        attendanceViewModel.attendanceViaImageStatus.collect { status ->
            when (status) {
                is ApiStates.Failure -> status.error.message?.let { snackbar.showSnackbar(it) }
                ApiStates.Loading -> snackbar.showSnackbar("Uploading selfie…")
                is ApiStates.Success -> snackbar.showSnackbar("Selfie uploaded for attendance approval")
                ApiStates.Idle -> Unit
            }
        }
    }

    val details by viewModel.conferences.collectAsStateWithLifecycle()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent
    ) { _ ->
        when (val d = details) {
            is ApiStates.Failure -> ErrorState(
                message = d.error.message ?: "The programme couldn't be loaded.",
                onRetry = { viewModel.getConferenceById(conference.id) },
                modifier = Modifier.fillMaxSize()
            )
            is ApiStates.Success -> d.data?.let { detail ->
                ProgrammeList(
                    conference = conference,
                    detail = detail,
                    eyebrow = eyebrow,
                    isGuestUser = isGuestUser,
                    allowFeedback = allowFeedback,
                    snackbar = snackbar,
                    onSessionClick = onSessionClick,
                    onFeedback = onFeedback,
                    onMarkAttendance = { s -> attendanceViewModel.markAttendanceViaLocation(conferenceId = conference.id, sessionId = s.id) },
                    onMarkAttendanceByImage = { s, img -> attendanceViewModel.markAttendanceViaImage(conferenceId = conference.id, sessionId = s.id, image = img) }
                )
            } ?: ErrorState(message = "No programme published yet.", modifier = Modifier.fillMaxSize())
            else -> SkeletonDetail(Modifier.fillMaxSize(), showHero = false)
        }
    }
}

@Composable
private fun ProgrammeList(
    conference: Conference,
    detail: ConferenceDetail,
    eyebrow: String,
    isGuestUser: Boolean,
    allowFeedback: Boolean,
    snackbar: SnackbarHostState,
    onSessionClick: ((Session) -> Unit)?,
    onFeedback: (Session) -> Unit,
    onMarkAttendance: (Session) -> Unit,
    onMarkAttendanceByImage: (Session, ByteArray) -> Unit
) {
    val days = detail.days.orEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "hero") {
            GradientHeroCard {
                GlassPill(eyebrow)
                Spacer(Modifier.height(10.dp))
                Text(
                    (conference.title ?: detail.title).orEmpty().trim(),
                    style = AppTheme.typography.headlineSmall.copy(color = Color.White)
                )
                Text(
                    "${days.size} day${if (days.size == 1) "" else "s"} · ${days.sumOf { it.sessions.orEmpty().size }} sessions",
                    style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.78f))
                )
            }
        }
        item(key = "facts") {
            SsplCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DisplayDates.range(conference.startDate ?: detail.startDate, conference.endDate ?: detail.endDate)
                        .takeIf { it.isNotBlank() }?.let { FactRow(Icons.Default.CalendarMonth, "Dates", it) }
                    val venue = detail.venue ?: conference.venue ?: days.firstOrNull()?.sessions?.firstOrNull()?.venue
                    listOfNotNull(venue?.title ?: venue?.name, venue?.address).filter { it.isNotBlank() }.joinToString(", ")
                        .takeIf { it.isNotBlank() }?.let { FactRow(Icons.Default.Place, "Venue", it) }
                }
                (conference.summary ?: detail.summary)?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(12.dp))
                    ExpandableText(it.trim(), maxLines = 4)
                }
            }
        }
        if (!detail.addOnSets.isNullOrEmpty()) {
            item(key = "addons") { AddOnSetsList(detail.addOnSets) }
        }
        days.forEachIndexed { i, day ->
            item(key = "day_$i") {
                Text(
                    "DAY ${i + 1}" + DisplayDates.medium(day.date).takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty(),
                    style = AppTheme.typography.overline,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }
            val rows = (day.breaks.orEmpty() + day.sessions.orEmpty()).sortedBy { it.startsAt ?: "" }
            rows.forEachIndexed { j, s ->
                item(key = "d${i}_${s.id}_$j") {
                    val showFeedback = allowFeedback && !s.isBreak && !isGuestUser &&
                        runCatching { isNowAfter(targetTime = s.endsAt, graceMinutes = 24 * 60) }.getOrDefault(false)
                    val showAttendance = !s.isBreak && !isGuestUser &&
                        runCatching { isInRange(startDateTimeUtc = s.startsAt, endDateTimeUtc = s.endsAt, graceMinutes = 15) }.getOrDefault(false)
                    SessionScheduleRow(
                        session = s,
                        onClick = if (s.isBreak || onSessionClick == null) null else { { onSessionClick(s) } },
                        footer = if (showFeedback || showAttendance) {
                            {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                                    if (showFeedback) {
                                        TextButtonLink("Give feedback", onClick = { onFeedback(s) })
                                    }
                                    if (showAttendance) {
                                        MarkAttendanceButton(
                                            apiCoordinates = Coordinates(latitude = s.venue?.lat ?: 0.0, longitude = s.venue?.lng ?: 0.0),
                                            onMarkAttendance = { onMarkAttendance(s) },
                                            scaffoldState = snackbar,
                                            onMarkAttendanceByImage = { onMarkAttendanceByImage(s, it) }
                                        )
                                    }
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceDialog(
    modifier: Modifier = Modifier,
    description: String = "Your selfie has been uploaded for attendance approval.",
    title: String = "Selfie uploaded",
    onDismiss: () -> Unit,
) {
    ConfirmDialog(title = title, message = description, confirmText = "OK", dismissText = "Close", onConfirm = onDismiss, onDismiss = onDismiss)
}
