package com.sspl.core.apis

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable


@Serializable
sealed class ConferenceRequest {
    @Serializable
    @Resource("/conferences/{conferenceId}")
    data class ById(
        val conferenceId: Int,
        val format: String = "days"
    ) : ConferenceRequest()

    @Serializable
    @Resource("/conferences/{conferenceId}")
    data class ByType(
        val conferenceId: Int,
        val format: String = "days"
    ) : ConferenceRequest()

    @Serializable
    @Resource("/conferences")
    data class GetAll(val type: String? = null) : ConferenceRequest()

    @Serializable
    @Resource("/conferences/{conferenceId}/sessions/{sessionId}/attendees")
    data class PostAttendance(
        val conferenceId: Long,
        val sessionId: Long,
    ) : ConferenceRequest()


}