package com.sspl.core.models

// All fields default: the live API omits keys freely (e.g. sessions[].venue_id, venue.lat/lng/radius).
// A required field that is missing fails decoding of the ENTIRE event list.

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConferenceDetail(
    @SerialName("id") val id: Long = 0,
    @SerialName("type") val type: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("add_on_sets") val addOnSets: List<AddOnSet>? = null,
    @SerialName("days") val days: List<Day>? = null,
    @SerialName("roles") val roles: List<Role>? = null,
    @SerialName("venue") val venue: Venue? = null
)

@Serializable
data class AddOnSet(
    @SerialName("title") val title: String? = null,
    @SerialName("add_ons") val addOns: List<AddOn>? = null
)

@Serializable
data class AddOn(
    @SerialName("title") val title: String? = null,
    @SerialName("subtitle") val subtitle: String? = null
)

@Serializable
data class Day(
    @SerialName("date") val date: String = "",
    @SerialName("breaks") val breaks: List<Session>? = null,
    @SerialName("sessions") val sessions: List<Session>? = null
)

@Serializable
data class Break(
    @SerialName("id") val id: Long = 0,
    @SerialName("title") val title: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("venue_id") val venueId: Int? = null,
    @SerialName("web_link") val webLink: String? = null,
    @SerialName("is_break") val isBreak: Boolean = false,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("add_on_sets") val addOnSets: List<AddOnSet>? = null,
    @SerialName("breakdown") val breakdown: List<Breakdown>? = null,
    @SerialName("venue") val venue: Venue? = null
)

@Serializable
data class Session(
    @SerialName("id") val id: Long = 0,
    @SerialName("title") val title: String = "",
    @SerialName("summary") val summary: String? = null,
    @SerialName("venue_id") val venueId: Int? = null,
    @SerialName("web_link") val webLink: String? = null,
    @SerialName("is_break") val isBreak: Boolean = false,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("add_on_sets") val addOnSets: List<AddOnSet>? = null,
    @SerialName("breakdown") val breakdown: List<Breakdown>? = null,
    @SerialName("venue") val venue: Venue? = null
)

@Serializable
data class Speaker(
    @SerialName("id") val id: Long = 0,
    @SerialName("email") val email: String = "",
    @SerialName("prefix") val prefix: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("gender") val gender: String = "",
) {
    fun fullName() = listOf(prefix, firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}

@Serializable
data class Venue(
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    @SerialName("radius") val radius: Int? = null
)

@Serializable
data class Breakdown(
    @SerialName("title") val title: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("is_break") val isBreak: Boolean = false,
    @SerialName("speakers") val speakers: List<Speaker> = emptyList(),
)