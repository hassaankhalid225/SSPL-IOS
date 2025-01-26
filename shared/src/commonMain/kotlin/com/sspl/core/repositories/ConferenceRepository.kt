package com.sspl.core.repositories

import com.sspl.core.apis.ConferenceRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.statement.HttpResponse

class ConferenceRepository(private val client: HttpClient) {

    suspend fun getConferenceById(conferenceId: Int): HttpResponse =
        client.get(ConferenceRequest.ById(conferenceId = conferenceId))

    suspend fun getAllConferences(): HttpResponse = client.get(ConferenceRequest.GetAll)


}