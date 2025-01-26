package com.sspl.core.apis

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable



@Serializable
sealed class ConferenceRequest {
    @Serializable
    @Resource("/conferences/{conferenceId}")
    data class ById(
        val conferenceId: Int
    ) : ConferenceRequest()

    @Serializable
    @Resource("/conferences")
    data object GetAll : ConferenceRequest()
}