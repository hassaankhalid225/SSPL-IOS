package com.sspl.core.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Conference(
    val id: Long = 0,
    // Live API also sends CONFERENCE / MONTHLY_MEETING / etc. Unknown values coerce to ALL
    // (requires coerceInputValues = true in the Json config) instead of failing the whole response.
    val type: ConferenceType = ConferenceType.ALL,
    val title: String? = null,
    val summary: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("venue") val venue: Venue? = null
) {
    fun isWorkshop(): Boolean = type == ConferenceType.WORKSHOP
}

@Serializable
enum class ConferenceType {
    ALL,
    WORKSHOP,
    PROGRAM,
    CONFERENCE,
    MONTHLY_MEETING,
    SEMINAR
}
