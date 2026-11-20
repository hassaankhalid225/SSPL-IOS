package com.sspl.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Display helpers for backend ISO dates like "2026-09-23T04:00:00.000Z" or "2026-09-23".
 * Pure, null-safe, never throw.
 */
@OptIn(ExperimentalTime::class)
object DisplayDates {
    private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun parse(iso: String?): LocalDate? {
        val s = iso?.trim()?.takeIf { it.length >= 10 } ?: return null
        return runCatching { LocalDate.parse(s.substring(0, 10)) }.getOrNull()
    }

    fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    fun day(iso: String?): String = parse(iso)?.day?.toString()?.padStart(2, '0') ?: "--"
    fun monthShort(iso: String?): String = parse(iso)?.let { MONTHS[it.month.number - 1] } ?: ""
    fun weekdayShort(iso: String?): String = parse(iso)?.let { DAYS[it.dayOfWeek.ordinal] } ?: ""

    /** "23 Sep 2026" */
    fun medium(iso: String?): String = parse(iso)?.let { "${it.day} ${MONTHS[it.month.number - 1]} ${it.year}" } ?: ""

    /** "23 – 25 Sep 2026", "30 Sep – 2 Oct 2026", or single date. */
    fun range(startIso: String?, endIso: String?): String {
        val a = parse(startIso) ?: return ""
        val b = parse(endIso)
        if (b == null || b == a) return medium(startIso)
        return if (a.year == b.year && a.month == b.month) {
            "${a.day} – ${b.day} ${MONTHS[a.month.number - 1]} ${a.year}"
        } else if (a.year == b.year) {
            "${a.day} ${MONTHS[a.month.number - 1]} – ${b.day} ${MONTHS[b.month.number - 1]} ${a.year}"
        } else "${medium(startIso)} – ${medium(endIso)}"
    }

    /** "Today", "Tomorrow", "In 5 days", "3 days ago", or "" when unknown. */
    fun relative(iso: String?): String {
        val d = parse(iso) ?: return ""
        val diff = today().daysUntil(d)
        return when {
            diff == 0 -> "Today"
            diff == 1 -> "Tomorrow"
            diff in 2..60 -> "In $diff days"
            diff == -1 -> "Yesterday"
            diff in -60..-2 -> "${-diff} days ago"
            else -> ""
        }
    }

    /** "Good morning" / "Good afternoon" / "Good evening" by local time. */
    fun greeting(): String {
        val h = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return when (h) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
