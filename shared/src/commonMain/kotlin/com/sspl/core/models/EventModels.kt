package com.sspl.core.models

import com.sspl.utils.DateTimeUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventRole(
    val id: Long = 0,
    val name: String = "",
    val fee: Double = 0.0,
    @SerialName("early_bird_fee") val earlyBirdFee: Double? = null,
    @SerialName("early_bird_deadline") val earlyBirdDeadline: String? = null,
    val description: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
) {
    val isEarlyBirdActive: Boolean
        get() {
            if (earlyBirdFee == null || earlyBirdDeadline.isNullOrBlank()) return false
            return try {
                val deadline = Instant.parse(earlyBirdDeadline)
                val now = Instant.fromEpochMilliseconds(DateTimeUtils.timeInMilliNow())
                now <= deadline
            } catch (e: Exception) {
                false
            }
        }

    val currentFee: Double
        get() = if (isEarlyBirdActive && earlyBirdFee != null) earlyBirdFee else fee
}

@Serializable
data class EventVenue(
    val id: Long? = null,
    val name: String? = null,
    val title: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
) {
    val displayAddress: String
        get() = address?.takeIf { it.isNotBlank() }
            ?: title?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: "Venue to be announced"
}

@Serializable
data class EventImage(
    val url: String = ""
)

@Serializable
data class EventItem(
    val id: Long = 0,
    @SerialName("_id") val mongoId: String? = null,
    val title: String = "",
    val summary: String? = null,
    val description: String? = null,
    val type: String? = null, // CONFERENCE | WORKSHOP | PROGRAM | MONTHLY_MEETING | etc.
    val status: String? = null, // scheduled | ongoing | completed | past | live | active | published
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("eventStart") val eventStart: String? = null,
    @SerialName("eventEnd") val eventEnd: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val images: List<EventImage> = emptyList(),
    val venue: EventVenue? = null,
    @SerialName("is_registration_enabled") val isRegistrationEnabled: Boolean = true,
    @SerialName("registration_open") val registrationOpen: Boolean = true,
    @SerialName("web_link") val webLink: String? = null,
    val roles: List<EventRole> = emptyList(),
    val sessions: List<Session> = emptyList(),
    @SerialName("add_on_sets") val addOnSets: List<AddOnSet> = emptyList()
) {
    val effectiveId: Long get() = id

    val effectiveStart: String? get() = startDate ?: eventStart
    val effectiveEnd: String? get() = endDate ?: eventEnd ?: effectiveStart

    val effectiveImage: String?
        get() = imageUrl?.takeIf { it.isNotBlank() }
            ?: images.firstOrNull()?.url?.takeIf { it.isNotBlank() }

    val effectiveDescription: String
        get() = description?.takeIf { it.isNotBlank() }
            ?: summary?.takeIf { it.isNotBlank() }
            ?: "Join SSP Lahore members for this professional surgical event. View full details and register."

    val effectiveVenue: String
        get() = venue?.displayAddress ?: "Venue to be announced"

    /**
     * Determine if the event is Ongoing, Upcoming, or Past
     */
    val computedStatus: String
        get() {
            val raw = (status ?: "").lowercase()
            if (raw == "completed" || raw == "past" || raw == "cancelled" || raw == "inactive") {
                return "past"
            }
            if (raw == "ongoing" || raw == "live") {
                return "ongoing"
            }

            val startIso = effectiveStart ?: return "upcoming"
            return try {
                val tz = TimeZone.of("Asia/Karachi")
                val nowInstant = Instant.fromEpochMilliseconds(DateTimeUtils.timeInMilliNow())
                val nowLocalDate = nowInstant.toLocalDateTime(tz).date
                val startDateParsed = Instant.parse(startIso).toLocalDateTime(tz).date

                val endIso = effectiveEnd ?: startIso
                val endDateParsed = Instant.parse(endIso).toLocalDateTime(tz).date

                when {
                    nowLocalDate > endDateParsed -> "past"
                    nowLocalDate >= startDateParsed && nowLocalDate <= endDateParsed -> "ongoing"
                    else -> "upcoming"
                }
            } catch (e: Exception) {
                if (raw.isNotBlank()) raw else "upcoming"
            }
        }

    val startDayNumber: String
        get() {
            val iso = effectiveStart ?: return "--"
            return try {
                val tz = TimeZone.of("Asia/Karachi")
                val dt = Instant.parse(iso).toLocalDateTime(tz)
                dt.dayOfMonth.toString()
            } catch (e: Exception) {
                try {
                    val parts = iso.split("T").first().split("-")
                    if (parts.size == 3) parts[2].trimStart('0') else "--"
                } catch (ex: Exception) {
                    "--"
                }
            }
        }

    val startMonthName: String
        get() {
            val iso = effectiveStart ?: return "TBA"
            return try {
                val tz = TimeZone.of("Asia/Karachi")
                val dt = Instant.parse(iso).toLocalDateTime(tz)
                dt.month.name.take(3).uppercase()
            } catch (e: Exception) {
                try {
                    val parts = iso.split("T").first().split("-")
                    val m = parts[1].toInt()
                    val months = listOf("", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
                    months.getOrElse(m) { "TBA" }
                } catch (ex: Exception) {
                    "TBA"
                }
            }
        }

    val startYear: String
        get() {
            val iso = effectiveStart ?: return ""
            return try {
                val tz = TimeZone.of("Asia/Karachi")
                val dt = Instant.parse(iso).toLocalDateTime(tz)
                dt.year.toString()
            } catch (e: Exception) {
                try {
                    iso.split("T").first().split("-").first()
                } catch (ex: Exception) {
                    ""
                }
            }
        }

    val formattedDateRange: String
        get() {
            val start = effectiveStart ?: return "Date to be announced"
            val end = effectiveEnd
            return if (end != null && end != start) {
                "$startDayNumber $startMonthName $startYear – ${formatSingleDate(end)}"
            } else {
                "$startDayNumber $startMonthName $startYear"
            }
        }

    val formattedTimeRange: String
        get() {
            val st = startTime?.takeIf { it.isNotBlank() }
            val et = endTime?.takeIf { it.isNotBlank() }
            return when {
                st != null && et != null -> "$st – $et"
                st != null -> st
                et != null -> et
                else -> ""
            }
        }

    val readableType: String
        get() = when (type?.uppercase()) {
            "CONFERENCE" -> "Conference"
            "WORKSHOP" -> "Workshop"
            "PROGRAM" -> "Scientific Program"
            "MONTHLY_MEETING" -> "Monthly Clinical Meeting"
            "MONTHLY_MEETING_MDT" -> "Clinical Meeting & MDT"
            "SEMINAR_SYMPOSIUM" -> "Seminar & Symposium"
            "FELLOWSHIP_TRAINING" -> "Fellowship & Training"
            "CME_CPD" -> "CME / CPD"
            else -> type?.replace("_", " ")?.capitalizeWords() ?: "Event"
        }

    val isRegistrationAllowed: Boolean
        get() = isRegistrationEnabled && registrationOpen && computedStatus != "past"
}

private fun formatSingleDate(iso: String): String {
    return try {
        val parts = iso.split("T").first().split("-")
        val y = parts[0]
        val m = parts[1].toInt()
        val d = parts[2].trimStart('0')
        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        "$d ${months.getOrElse(m) { "" }} $y"
    } catch (e: Exception) {
        iso
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

@Serializable
data class EventCheckData(
    @SerialName("is_registered") val isRegistered: Boolean = false,
    @SerialName("can_register") val canRegister: Boolean = true,
    val registration: Registration? = null
)

@Serializable
data class EventCheckResponse(
    val success: Boolean = true,
    val data: EventCheckData? = null
)

@Serializable
data class EventPaymentInfo(
    val required: Boolean = false,
    @SerialName("paymentUrl") val paymentUrlCamel: String? = null,
    @SerialName("payment_url") val paymentUrlSnake: String? = null,
    @SerialName("transactionId") val transactionIdCamel: String? = null,
    @SerialName("transaction_id") val transactionIdSnake: String? = null,
    @SerialName("pending_approval") val pendingApproval: Boolean = false
) {
    val bestPaymentUrl: String? get() = paymentUrlSnake ?: paymentUrlCamel
    val bestTransactionId: String? get() = transactionIdSnake ?: transactionIdCamel
}

@Serializable
data class EventRegisterResponse(
    val success: Boolean = true,
    val message: String? = null,
    val registration: Registration? = null,
    val payment: EventPaymentInfo? = null
)

@Serializable
data class EventRetryPaymentResponse(
    val success: Boolean = true,
    val message: String? = null,
    val payment: EventPaymentInfo? = null
)

@Serializable
data class SwichVerifyStatusRequest(
    val customerTransactionId: String
)

@Serializable
data class SwichVerifyStatusResponse(
    val success: Boolean = true,
    val status: String? = null, // "success" | "failed" | "pending"
    val message: String? = null
)

@Serializable
data class ArchiveConference(
    val id: Long = 0,
    val title: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val type: String? = null
)

@Serializable
data class ArchiveMaterial(
    val id: Long = 0,
    val title: String? = null,
    val type: String? = null, // IMAGE | POSTER | PRESENTATION | ABSTRACT | ABSTRACT_BOOK | DOCUMENT
    @SerialName("file_url") val fileUrl: String? = null
)

@Serializable
data class EventArchiveRecord(
    val id: Long = 0,
    val conference: ArchiveConference? = null,
    val notes: String? = null,
    val materials: List<ArchiveMaterial> = emptyList()
)

@Serializable
data class EventArchiveResponse(
    val success: Boolean = true,
    val data: List<EventArchiveRecord> = emptyList()
)

