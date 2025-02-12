package com.sspl.core.apis

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable


@Serializable
sealed class ConferenceRequest {
    @Serializable
    @Resource("/conferences/{conferenceId}")
    data class ById(
        val conferenceId: Long,
        val format: String = "days"
    ) : ConferenceRequest()

    @Serializable
    @Resource("/conferences/{conferenceId}")
    data class ByType(
        val conferenceId: Long,
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

    @Serializable
    @Resource("/conferences/{conferenceId}/feedback-form")
    data class GetFeedbackForm(
        val conferenceId: Long
    ) : ConferenceRequest()
    @Serializable
    @Resource("/conferences/{conferenceId}/feedback")
    data class PostFeedbackFormRequest(
        val conferenceId: Long
    ) : ConferenceRequest()
    @Serializable
    @Resource("/conferences/{conferenceId}/sessions/{sessionId}/feedback")
    data class PostFeedbackFormSessionRequest(
        val conferenceId: Long,
        val sessionId: Long
    ) : ConferenceRequest()


}