package com.sspl.ui.conference

import androidx.compose.runtime.Immutable
import com.sspl.core.models.EventItem
import com.sspl.utils.DisplayDates

/**
 * Display-ready event row. Everything that needs date parsing (status, day/month blocks,
 * ranges) is computed ONCE off the main thread when the list arrives, so list items never
 * call the parsing getters on [EventItem] during scroll / recomposition.
 *
 * `image` is the same String reference the repository holds (base64 data URIs are several
 * hundred KB each), so this does not copy image data.
 */
@Immutable
data class EventCardUi(
    val id: Long,
    val title: String,
    val type: String,
    val typeLabel: String,
    val status: String, // upcoming | ongoing | past
    val image: String?,
    val day: String,
    val month: String,
    val year: String,
    val dateRange: String,
    val timeRange: String,
    val venue: String,
    val summary: String,
    val relative: String,
    val registrationOpen: Boolean,
    val startKey: String,
    val searchBlob: String
)

fun EventItem.toCardUi(): EventCardUi {
    // Web: an event whose active_status isn't ACTIVE (or is_active=false) is treated as past.
    val inactive = (activeStatus != null && !activeStatus.equals("ACTIVE", ignoreCase = true)) || !isActive
    val status = if (inactive) "past" else computedStatus
    return EventCardUi(
        id = effectiveId,
        title = title.ifBlank { "Untitled event" },
        type = (type ?: "").uppercase(),
        typeLabel = readableType,
        status = status,
        image = effectiveImage,
        day = startDayNumber,
        month = startMonthName,
        year = startYear,
        dateRange = formattedDateRange,
        timeRange = formattedTimeRange,
        venue = effectiveVenue,
        summary = (summary?.takeIf { it.isNotBlank() } ?: description?.takeIf { it.isNotBlank() } ?: "").take(220),
        relative = if (status == "upcoming") DisplayDates.relative(effectiveStart) else "",
        registrationOpen = isRegistrationEnabled && registrationOpen && status != "past",
        startKey = effectiveStart ?: "",
        searchBlob = listOf(title, effectiveVenue, summary.orEmpty(), readableType).joinToString(" ").lowercase()
    )
}

/** Status tabs, same three as the web events page. */
enum class EventStatusTab(val key: String, val label: String) {
    UPCOMING("upcoming", "Upcoming"),
    ONGOING("ongoing", "Ongoing"),
    PAST("past", "Past")
}

/** Category chips (Events tab). Type matching mirrors the web filters. */
enum class EventCategory(val label: String) {
    ALL("All"),
    CONFERENCE("Conferences"),
    WORKSHOP("Workshops"),
    MONTHLY_MEETING("Monthly meetings"),
    SEMINAR("Seminars");

    fun matches(type: String): Boolean = when (this) {
        ALL -> true
        CONFERENCE -> type.contains("CONFERENCE") || type.contains("PROGRAM")
        WORKSHOP -> type.contains("WORKSHOP")
        MONTHLY_MEETING -> type.contains("MONTHLY") || type.contains("MEETING")
        SEMINAR -> type.contains("SEMINAR") || type.contains("SYMPOSIUM") || type.contains("CME")
    }
}

/**
 * Which slice of /external/events a screen shows. Mirrors the web:
 * `/events` = everything, `/conferences` = PROGRAM + CONFERENCE, `/conferences/workshops` = WORKSHOP.
 */
enum class EventScope(val allowedTypes: Set<String>?) {
    ALL(null),
    CONFERENCES(setOf("PROGRAM", "CONFERENCE")),
    WORKSHOPS(setOf("WORKSHOP"));

    fun includes(type: String) = allowedTypes == null || type in allowedTypes
}

/** Readable message for a failed load; never surfaces raw exception class names. */
internal fun Throwable.toUserMessage(fallback: String): String {
    val m = message.orEmpty()
    return when {
        m.contains("timeout", ignoreCase = true) || m.contains("timed out", ignoreCase = true) ->
            "The server is taking too long to respond. Check your connection and try again."
        m.contains("Unable to resolve host", ignoreCase = true) || m.contains("UnknownHost", ignoreCase = true) ||
            m.contains("network", ignoreCase = true) || m.contains("connect", ignoreCase = true) ->
            "You appear to be offline. Check your connection and try again."
        else -> fallback
    }
}

/** Format a fee for display. */
internal fun formatPkr(amount: Double): String =
    if (amount <= 0.0) "Free" else "PKR " + amount.toLong().toString().reversed().chunked(3).joinToString(",").reversed()
