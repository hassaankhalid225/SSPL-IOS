package com.sspl.core.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/**
 * Participant state from GET /conferences/{id} (web ConferenceView / SessionActionButtons /
 * CertificateDownload). Parsed leniently from JSON because the shape varies by event type.
 */
data class ConferenceParticipation(
    val sessions: Map<Long, SessionParticipation>,
    /** Conference-level certificate (web `selectedConference.certificate`). */
    val certificateUrl: String?,
    val certificateSerial: String?,
    val hasCertificate: Boolean,
    /** Web `certificate_access` ({allowed, status, reason}); null when absent. */
    val certificateAccess: CertificateAccess?,
    val feedbackSubmitted: Boolean,
    val hasAcceptedAttendance: Boolean
)

data class CertificateAccess(val allowed: Boolean?, val status: String?, val reason: String?)

data class RawWindow(val isActive: Boolean, val opensAt: String?, val closesAt: String?, val forced: Boolean?)

data class SessionParticipation(
    val sessionId: Long,
    val isBreak: Boolean,
    val startsAt: String?,
    val endsAt: String?,
    /** Upper-cased `attendance.status` or `attendance_status` (ACCEPTED / PENDING / REJECTED …). */
    val attendanceStatus: String?,
    val feedbackSubmitted: Boolean,
    val attendanceWindow: RawWindow?,
    val attendanceForced: Boolean?,
    val attendanceOpensAt: String?,
    val attendanceClosesAt: String?,
    val feedbackWindow: RawWindow?,
    val feedbackForced: Boolean?,
    val feedbackOpensAt: String?,
    val feedbackClosesAt: String?
)

/** Result of the web's `computeWindow` (times in epoch millis). */
data class ActionWindow(val isActive: Boolean, val opensAt: Long?, val closesAt: Long?, val forced: Boolean?)

object SessionWindows {
    private const val HOUR_MS = 60L * 60 * 1000
    private const val DAY_MS = 24 * HOUR_MS

    /** JS `new Date(value)`: ISO with offset, or a local date-time without one. Null when invalid. */
    fun toMillis(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val v = value.trim()
        runCatching { return Instant.parse(v).toEpochMilliseconds() }
        return runCatching {
            LocalDateTime.parse(v.replace(' ', 'T').substringBefore('Z'))
                .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }.getOrNull() ?: runCatching {
            kotlinx.datetime.LocalDate.parse(v.take(10)).let { LocalDateTime(it.year, it.month, it.day, 0, 0) }
                .toInstant(TimeZone.UTC).toEpochMilliseconds()
        }.getOrNull()
    }

    private fun compute(forced: Boolean?, manualOpen: Long?, manualClose: Long?, autoOpen: Long?, autoClose: Long?, now: Long): ActionWindow {
        val opensAt = manualOpen ?: autoOpen
        val closesAt = manualClose ?: autoClose
        if (forced == false) return ActionWindow(false, opensAt, closesAt, false)
        if (forced == true) return ActionWindow(true, opensAt, closesAt, true)
        val active = when {
            opensAt != null && closesAt != null -> now in opensAt..closesAt
            opensAt != null -> now >= opensAt
            closesAt != null -> now <= closesAt
            else -> false
        }
        return ActionWindow(active, opensAt, closesAt, null)
    }

    /** Web getAttendanceWindowLocal: server window, else overrides, else start → start + 1 h. */
    fun attendance(s: SessionParticipation, now: Long): ActionWindow {
        s.attendanceWindow?.let { return ActionWindow(it.isActive, toMillis(it.opensAt), toMillis(it.closesAt), it.forced) }
        val start = toMillis(s.startsAt)
        return compute(s.attendanceForced, toMillis(s.attendanceOpensAt), toMillis(s.attendanceClosesAt), start, start?.plus(HOUR_MS), now)
    }

    /** Web getFeedbackWindowLocal: server window, else overrides, else end + 1 day → end + 2 days. */
    fun feedback(s: SessionParticipation, now: Long): ActionWindow {
        s.feedbackWindow?.let { return ActionWindow(it.isActive, toMillis(it.opensAt), toMillis(it.closesAt), it.forced) }
        val end = toMillis(s.endsAt)
        return compute(s.feedbackForced, toMillis(s.feedbackOpensAt), toMillis(s.feedbackClosesAt), end?.plus(DAY_MS), end?.plus(2 * DAY_MS), now)
    }
}

object ParticipationParser {

    private fun JsonObject.prim(key: String): JsonPrimitive? = (this[key] as? JsonPrimitive)?.takeIf { it !is JsonNull }
    private fun JsonObject.str(key: String): String? = prim(key)?.contentOrNull?.takeIf { it.isNotBlank() }
    private fun JsonObject.long(key: String): Long? = prim(key)?.let { it.longOrNull ?: it.contentOrNull?.toDoubleOrNull()?.toLong() }
    private fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject

    /** JS truthiness (`Boolean(x)`). */
    private fun JsonElement?.truthy(): Boolean = when (this) {
        null, is JsonNull -> false
        is JsonPrimitive -> booleanOrNull ?: if (isString) content.isNotEmpty() else (content != "0" && content != "0.0")
        else -> true
    }

    /** Strict `=== true` / `=== false`; anything else is null. */
    private fun JsonObject.strictBool(key: String): Boolean? {
        val p = prim(key) ?: return null
        if (p.isString) return null
        return p.booleanOrNull
    }

    private fun window(o: JsonObject?): RawWindow? = o?.let {
        RawWindow(
            isActive = it["is_active"].truthy(),
            opensAt = it.str("opens_at"),
            closesAt = it.str("closes_at"),
            forced = it.strictBool("forced")
        )
    }

    private fun session(o: JsonObject): SessionParticipation? {
        val id = o.long("id") ?: return null
        val status = o.obj("attendance")?.str("status") ?: o.str("attendance_status")
        return SessionParticipation(
            sessionId = id,
            isBreak = o["is_break"].truthy(),
            startsAt = o.str("starts_at"),
            endsAt = o.str("ends_at"),
            attendanceStatus = status?.uppercase(),
            feedbackSubmitted = o["feedback_submitted"].truthy(),
            attendanceWindow = window(o.obj("attendance_window")),
            attendanceForced = o.strictBool("attendance_forced"),
            attendanceOpensAt = o.str("attendance_opens_at"),
            attendanceClosesAt = o.str("attendance_closes_at"),
            feedbackWindow = window(o.obj("feedback_window")),
            feedbackForced = o.strictBool("feedback_forced"),
            feedbackOpensAt = o.str("feedback_opens_at"),
            feedbackClosesAt = o.str("feedback_closes_at")
        )
    }

    fun parse(root: JsonElement): ConferenceParticipation {
        val top = root as? JsonObject ?: JsonObject(emptyMap())
        // Unwrapped like the web; tolerate a `data` envelope too.
        val c = if (top["sessions"] == null && top["days"] == null) top.obj("data") ?: top else top
        val raw = buildList {
            (c["sessions"] as? JsonArray)?.forEach { (it as? JsonObject)?.let(::add) }
            (c["days"] as? JsonArray)?.forEach { d ->
                ((d as? JsonObject)?.get("sessions") as? JsonArray)?.forEach { (it as? JsonObject)?.let(::add) }
            }
        }
        val sessions = raw.mapNotNull(::session).associateBy { it.sessionId }
        val cert = c["certificate"]
        val certObj = cert as? JsonObject
        val access = c.obj("certificate_access")?.let {
            CertificateAccess(allowed = it.strictBool("allowed"), status = it.str("status"), reason = it.str("reason"))
        }
        return ConferenceParticipation(
            sessions = sessions,
            certificateUrl = certObj?.let { it.str("fileUrl") ?: it.str("file_url") },
            certificateSerial = certObj?.let { it.str("serialNumber") ?: it.str("serial_number") },
            hasCertificate = cert.truthy(),
            certificateAccess = access,
            feedbackSubmitted = c["feedback_submitted"].truthy(),
            hasAcceptedAttendance = c["has_accepted_attendance"].truthy() ||
                sessions.values.any { it.attendanceStatus == "ACCEPTED" }
        )
    }
}
