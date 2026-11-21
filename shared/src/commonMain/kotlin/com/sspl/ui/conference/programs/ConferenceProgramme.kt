package com.sspl.ui.conference.programs

import com.sspl.designsystem.icons.SsplIcons

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.models.Session
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.ExpandableText
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.MarkAttendanceButton
import com.sspl.ui.conference.AttendanceViewModel
import com.sspl.ui.conference.ConferenceDetailViewModel
import com.sspl.ui.conference.components.AddOnSetsList
import com.sspl.ui.conference.components.SessionScheduleRow
import com.sspl.ui.portal.portalViewModel
import com.sspl.utils.DisplayDates
import dev.jordond.compass.Coordinates
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Clock

/**
 * Shared body for the conference programme / workshop detail (GET /conferences/{id}?days).
 * Per session, like web SessionActionButtons + CertificateDownload: attendance (GPS → selfie
 * fallback) inside its window, feedback inside its window, and the certificate once unlocked.
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
    val actionsViewModel: SessionActionsViewModel = portalViewModel { SessionActionsViewModel(it.get(), it.get(), it.get()) }
    val urlOpener: UrlOpener = koinInject()
    val showLocationSuccess by attendanceViewModel.showAttendanceDialogByLocation.collectAsStateWithLifecycle()
    val showImageSuccess by attendanceViewModel.showAttendanceDialogByImage.collectAsStateWithLifecycle()
    // Session whose attendance request is in flight (AttendanceViewModel doesn't echo it back).
    var markingSessionId by remember { mutableStateOf<Long?>(null) }

    if (showLocationSuccess) {
        AttendanceDialog(title = "Attendance marked", description = "Your attendance has been marked.", onDismiss = attendanceViewModel::onDismissDialogs)
    }
    if (showImageSuccess) {
        AttendanceDialog(onDismiss = attendanceViewModel::onDismissDialogs)
    }

    LaunchedEffect(conference.id) { viewModel.getConferenceById(conference.id) }
    // Re-runs whenever the screen re-enters composition (e.g. back from feedback) to pick up new state.
    LaunchedEffect(conference.id, isGuestUser) { if (!isGuestUser) actionsViewModel.load(conference.id) }
    LaunchedEffect(Unit) {
        attendanceViewModel.attendanceViaLocationStatus.collect { status ->
            when (status) {
                is ApiStates.Failure -> status.error.message.takeIf { it.isNotBlank() }?.let { snackbar.showSnackbar(it) }
                ApiStates.Loading -> snackbar.showSnackbar("Marking attendance…")
                is ApiStates.Success -> {
                    markingSessionId?.let { actionsViewModel.onAttendanceMarked(it, status.data?.status) }
                    actionsViewModel.load(conference.id)
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
                    markingSessionId?.let { actionsViewModel.onAttendanceMarked(it, status.data?.status ?: "PENDING") }
                    actionsViewModel.load(conference.id)
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
    // Windows open/close with time (web re-evaluates every minute).
    var now by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = Clock.System.now().toEpochMilliseconds()
        }
    }

    val details by viewModel.conferences.collectAsStateWithLifecycle()
    val actions by actionsViewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent
    ) { _ ->
        when (val d = details) {
            is ApiStates.Failure -> ErrorState(
                message = d.error.message.ifBlank { "The programme couldn't be loaded." },
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
                    actions = actions,
                    actionUi = { s -> actionsViewModel.uiFor(actions, s, now) },
                    onSessionClick = onSessionClick,
                    onFeedback = onFeedback,
                    onMarkAttendance = { s, lat, lng ->
                        markingSessionId = s.id
                        attendanceViewModel.markAttendanceViaLocation(conferenceId = conference.id, sessionId = s.id, lat = lat, lng = lng)
                    },
                    onMarkAttendanceByImage = { s, img ->
                        markingSessionId = s.id
                        attendanceViewModel.markAttendanceViaImage(conferenceId = conference.id, sessionId = s.id, image = img)
                    },
                    onCertificate = { s -> actionsViewModel.getCertificate(conference.id, s.id) }
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
    actions: SessionActionsState,
    actionUi: (Session) -> SessionActionUi,
    onSessionClick: ((Session) -> Unit)?,
    onFeedback: (Session) -> Unit,
    onMarkAttendance: (Session, Double, Double) -> Unit,
    onMarkAttendanceByImage: (Session, ByteArray) -> Unit,
    onCertificate: (Session) -> Unit
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
                        .takeIf { it.isNotBlank() }?.let { FactRow(SsplIcons.CalendarMonth, "Dates", it) }
                    val venue = detail.venue ?: conference.venue ?: days.firstOrNull()?.sessions?.firstOrNull()?.venue
                    listOfNotNull(venue?.title ?: venue?.name, venue?.address).filter { it.isNotBlank() }.joinToString(", ")
                        .takeIf { it.isNotBlank() }?.let { FactRow(SsplIcons.Place, "Venue", it) }
                }
                (conference.summary ?: detail.summary)?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(12.dp))
                    ExpandableText(it.trim(), maxLines = 4)
                }
            }
        }
        if (!isGuestUser && actions.needsPayment && actions.isRegistered == true) {
            item(key = "payDue") {
                SsplCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(SsplIcons.Info, contentDescription = null, tint = AppTheme.colors.warning, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Payment is pending for this registration. Attendance will be available after payment is completed.",
                            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                        )
                    }
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
                    val showActions = !s.isBreak && !isGuestUser
                    SessionScheduleRow(
                        session = s,
                        onClick = if (s.isBreak || onSessionClick == null) null else { { onSessionClick(s) } },
                        footer = if (showActions) {
                            {
                                SessionActions(
                                    session = s,
                                    ui = actionUi(s),
                                    isRegistered = actions.isRegistered != false,
                                    needsPayment = actions.needsPayment,
                                    allowFeedback = allowFeedback,
                                    certificateLoading = actions.certificateLoadingId == s.id,
                                    showCertificate = actions.participation != null,
                                    snackbar = snackbar,
                                    onFeedback = { onFeedback(s) },
                                    onMarkAttendance = { lat, lng -> onMarkAttendance(s, lat, lng) },
                                    onMarkAttendanceByImage = { onMarkAttendanceByImage(s, it) },
                                    onCertificate = { onCertificate(s) }
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

/** Attendance · feedback · certificate controls for one session (web SessionActionButtons + CertificateDownload). */
@Composable
private fun SessionActions(
    session: Session,
    ui: SessionActionUi,
    isRegistered: Boolean,
    needsPayment: Boolean,
    allowFeedback: Boolean,
    certificateLoading: Boolean,
    showCertificate: Boolean,
    snackbar: SnackbarHostState,
    onFeedback: () -> Unit,
    onMarkAttendance: (Double, Double) -> Unit,
    onMarkAttendanceByImage: (ByteArray) -> Unit,
    onCertificate: () -> Unit
) {
    val colors = AppTheme.colors
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!isRegistered) {
            Text("Register for this event first, then mark attendance.", style = AppTheme.typography.bodySmall)
            return@Column
        }
        // Attendance
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ATTENDANCE", style = AppTheme.typography.overline)
                Text(ui.attendanceSubtext, style = AppTheme.typography.bodySmall)
            }
            when (ui.status) {
                "ACCEPTED" -> StatusBadge("Marked", variant = BadgeVariant.SUCCESS)
                "PENDING" -> StatusBadge("Pending approval", variant = BadgeVariant.WARNING)
                else -> MarkAttendanceButton(
                    apiCoordinates = Coordinates(latitude = session.venue?.lat ?: 0.0, longitude = session.venue?.lng ?: 0.0),
                    onMarkAttendance = onMarkAttendance,
                    scaffoldState = snackbar,
                    onMarkAttendanceByImage = onMarkAttendanceByImage,
                    enabled = ui.attendanceActive && !needsPayment,
                    radiusInMeters = session.venue?.radius?.takeIf { it > 0 }?.toDouble() ?: 50.0,
                    label = if (ui.status == "REJECTED") "Mark again" else "Mark attendance"
                )
            }
        }
        // Feedback (shown once attendance is accepted or pending, like the web).
        if (allowFeedback && ui.showFeedbackControls) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("FEEDBACK", style = AppTheme.typography.overline)
                    Text(ui.feedbackSubtext, style = AppTheme.typography.bodySmall)
                }
                if (ui.feedbackSubmitted) {
                    StatusBadge("Submitted", variant = BadgeVariant.SUCCESS)
                } else {
                    TextButtonLink(
                        "Give feedback",
                        onClick = onFeedback,
                        enabled = ui.feedbackActive,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
        // Certificate
        if (showCertificate && (ui.certificateUnlocked || ui.status != null)) {
            if (ui.certificateUnlocked) {
                ui.certificateStatusLabel?.let { StatusBadge(it, variant = BadgeVariant.SUCCESS) }
                SecondaryButton(
                    text = "Get certificate",
                    onClick = onCertificate,
                    loading = certificateLoading,
                    icon = SsplIcons.WorkspacePremium
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(SsplIcons.Lock, contentDescription = "Certificate locked", tint = colors.muted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(ui.certificateLockedMessage, style = AppTheme.typography.bodySmall)
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
