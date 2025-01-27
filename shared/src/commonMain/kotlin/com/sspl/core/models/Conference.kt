package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conference(
    val id: Int,
    val title: String,
    val summary: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("days") val days: List<Day>? = null
)

@Serializable
data class Day(
    @SerialName("date") val date: String,
    @SerialName("breaks") val breaks: List<Session>,
    @SerialName("sessions") val sessions: List<Session>
)

@Serializable
data class Session(
    @SerialName("title") val title: String,
    @SerialName("summary") val summary: String?,
    @SerialName("venue_id") val venueId: Int?,
    @SerialName("web_link") val webLink: String?,
    @SerialName("is_break") val isBreak: Boolean?,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("speakers") val speakers: List<Speaker>,
    @SerialName("venue") val venue: Venue?
)

@Serializable
data class Speaker(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("prefix") val prefix: String?,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("gender") val gender: String
)

@Serializable
data class Venue(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("address") val address: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)
