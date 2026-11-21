package com.sspl.ui.conference.programs

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.ActionWindow
import com.sspl.core.models.ConferenceParticipation
import com.sspl.core.models.ParticipationParser
import com.sspl.core.models.Session
import com.sspl.core.models.SessionParticipation
import com.sspl.core.models.SessionWindows
import com.sspl.core.repositories.ConferenceRepository
import com.sspl.core.repositories.PortalRepository
import com.sspl.session.UserSession
import com.sspl.ui.portal.resolvePortalFileUrl
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.datetime.toLocalDateTime

@Immutable
data class SessionActionsState(
    val participation: ConferenceParticipation? = null,
    /** Null until my-registrations loads (or if it fails) — then nothing is blocked on it. */
    val isRegistered: Boolean? = null,
    /** Web ConferenceView `needsPayment`: fee > 0 and payment not SUCCESS. */
    val needsPayment: Boolean = false,
    /** Status returned by a just-marked attendance (web `localStatus`). */
    val localAttendance: Map<Long, String> = emptyMap(),
    val certificateLoadingId: Long? = null
)

sealed interface SessionActionsEvent {
    data class OpenUrl(val url: String) : SessionActionsEvent
    data class Message(val text: String) : SessionActionsEvent
}

/** Everything one session row needs to render attendance / feedback / certificate (web parity). */
data class SessionActionUi(
    val status: String?,
    val attendanceWindow: ActionWindow,
    val feedbackWindow: ActionWindow,
    val feedbackSubmitted: Boolean,
    val attendanceSubtext: String,
    val feedbackSubtext: String,
    val attendanceActive: Boolean,
    val showFeedbackControls: Boolean,
    val feedbackActive: Boolean,
    val certificateUnlocked: Boolean,
    val certificateLockedMessage: String,
    val certificateStatusLabel: String?
)

/**
 * Participant actions for the programme: GET /conferences/{id} (attendance status, windows,
 * feedback flags, certificate access), registration payment state, and
 * POST /conferences/certificate/generate.
 */
class SessionActionsViewModel(
    private val conferenceRepository: ConferenceRepository,
    private val portalRepository: PortalRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(SessionActionsState())
    val state: StateFlow<SessionActionsState> = _state.asStateFlow()

    private val _events = Channel<SessionActionsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun load(conferenceId: Long) {
        viewModelScope.launch {
            val participation = async {
                runCatching {
                    val resp = conferenceRepository.getConferenceParticipation(conferenceId)
                    if (!resp.status.isSuccess()) return@runCatching null
                    val text = resp.bodyAsText()
                    withContext(Dispatchers.Default) { ParticipationParser.parse(json.parseToJsonElement(text)) }
                }.getOrNull()
            }
            val registration = async {
                portalRepository.getMyRegistrations().getOrNull()?.let { all ->
                    val mine = all.filter { it.conferenceId == conferenceId }
                    val reg = mine.firstOrNull { it.isConfirmed } ?: mine.firstOrNull()
                    if (reg == null) Pair(false, false)
                    else Pair(true, reg.feeAmount > 0.0 && !reg.paymentStatus.equals("SUCCESS", true))
                }
            }
            val p = participation.await()
            val r = registration.await()
            _state.update {
                it.copy(
                    participation = p ?: it.participation,
                    isRegistered = r?.first ?: it.isRegistered,
                    needsPayment = r?.second ?: it.needsPayment,
                    // Server state wins once it reflects the mark.
                    localAttendance = if (p != null) it.localAttendance.filterKeys { id -> p.sessions[id]?.attendanceStatus == null } else it.localAttendance
                )
            }
        }
    }

    fun onAttendanceMarked(sessionId: Long, status: String?) {
        _state.update {
            it.copy(localAttendance = it.localAttendance + (sessionId to (status?.uppercase()?.ifBlank { null } ?: "ACCEPTED")))
        }
    }

    /** Web CertificateDownload.handleDownload. */
    fun getCertificate(conferenceId: Long, sessionId: Long) {
        if (_state.value.certificateLoadingId != null) return
        viewModelScope.launch {
            val p = _state.value.participation
            p?.certificateUrl?.let { url ->
                resolvePortalFileUrl(url)?.let { _events.send(SessionActionsEvent.OpenUrl(it)) }
                return@launch
            }
            _state.update { it.copy(certificateLoadingId = sessionId) }
            val personId = userSession.getUser()?.id?.takeIf { it != 0L }
            portalRepository.generateCertificate(sessionId, personId, conferenceId)
                .onSuccess { data ->
                    val fileUrl = data.certificate?.fileUrl ?: data.url ?: data.downloadUrl
                    val resolved = resolvePortalFileUrl(fileUrl)
                    if (resolved != null) _events.send(SessionActionsEvent.OpenUrl(resolved))
                    else _events.send(SessionActionsEvent.Message(data.message ?: "Certificate URL not found in response."))
                }
                .onFailure { e ->
                    _events.send(SessionActionsEvent.Message(e.message?.takeIf { it.isNotBlank() } ?: "Failed to download certificate."))
                }
            _state.update { it.copy(certificateLoadingId = null) }
        }
    }

    /** Port of SessionActionButtons + CertificateDownload state for one session. */
    fun uiFor(s: SessionActionsState, session: Session, now: Long): SessionActionUi {
        val server = s.participation?.sessions?.get(session.id)
        val p = server ?: SessionParticipation(
            sessionId = session.id, isBreak = session.isBreak, startsAt = session.startsAt, endsAt = session.endsAt,
            attendanceStatus = null, feedbackSubmitted = false,
            attendanceWindow = null, attendanceForced = null, attendanceOpensAt = null, attendanceClosesAt = null,
            feedbackWindow = null, feedbackForced = null, feedbackOpensAt = null, feedbackClosesAt = null
        )
        val status = s.localAttendance[session.id] ?: p.attendanceStatus
        val accepted = status == "ACCEPTED"
        val aw = SessionWindows.attendance(p, now)
        val fw = SessionWindows.feedback(p, now)
        val feedbackSubmitted = p.feedbackSubmitted
        val showFeedback = accepted || status == "PENDING"

        val attendanceSubtext = when {
            accepted -> "Marked"
            status == "PENDING" -> "Pending approval"
            status == "REJECTED" -> "Rejected"
            aw.forced == false -> "Deactivated by admin"
            aw.isActive -> "Active now · Deactivates after 1 hour"
            aw.opensAt != null && now < aw.opensAt -> "Opens ${ProgrammeTime.short(aw.opensAt)}"
            aw.closesAt != null && now > aw.closesAt -> "Closed"
            else -> "Not active"
        }
        val feedbackSubtext = when {
            feedbackSubmitted -> "Submitted"
            !showFeedback -> "Available after attendance"
            fw.forced == false -> "Deactivated by admin"
            fw.isActive -> "Active · Required for certification"
            else -> "Feedback will be activated 24 hours after the session is completed."
        }

        // CertificateDownload: conference-level feedback also counts (ConferenceView passes it in).
        val conf = s.participation
        val feedbackDone = feedbackSubmitted || conf?.feedbackSubmitted == true
        val access = conf?.certificateAccess
        val accessAllowed = access?.allowed == true || (access == null && accepted && feedbackDone)
        val unlocked = conf?.hasCertificate == true || accessAllowed
        val locked = access?.reason ?: when {
            !accepted -> "Step 1: Mark attendance and wait for acceptance."
            !feedbackDone -> "Step 2: Submit session feedback to unlock your certificate."
            else -> "Certificate awaiting administrator release."
        }
        return SessionActionUi(
            status = status,
            attendanceWindow = aw,
            feedbackWindow = fw,
            feedbackSubmitted = feedbackSubmitted,
            attendanceSubtext = attendanceSubtext,
            feedbackSubtext = feedbackSubtext,
            attendanceActive = aw.isActive && !accepted && status != "PENDING",
            showFeedbackControls = showFeedback,
            feedbackActive = showFeedback && fw.isActive && !feedbackSubmitted,
            certificateUnlocked = unlocked,
            certificateLockedMessage = locked,
            certificateStatusLabel = if (access?.status == "AVAILABLE") "Available for download" else access?.status
        )
    }
}

/** Short local date-time like the web's `formatShort` (e.g. "12 Mar, 9:30 AM"). */
internal object ProgrammeTime {
    private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    fun short(millis: Long): String {
        val l = kotlin.time.Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        val h12 = if (l.hour % 12 == 0) 12 else l.hour % 12
        val ampm = if (l.hour < 12) "AM" else "PM"
        return "${l.day} ${MONTHS[l.month.ordinal]}, $h12:${l.minute.toString().padStart(2, '0')} $ampm"
    }
}
