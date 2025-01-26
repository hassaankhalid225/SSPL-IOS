package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Break(
    val title: String?,
    val summary: String?,
    @SerialName("venue_id") val venueId: Int?,
    @SerialName("web_link") val webLink: String?,
    @SerialName("is_break") val isBreak: Boolean?,
    @SerialName("starts_at") val startsAt: String?,
    @SerialName("ends_at") val endsAt: String?,
    val speakers: List<Speaker>?,
    val venue: Venue?
)