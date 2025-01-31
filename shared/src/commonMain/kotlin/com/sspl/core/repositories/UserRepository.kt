package com.sspl.core.repositories

import com.sspl.core.apis.ProfileRequest
import com.sspl.core.models.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 21/01/2025.
 * se.muhammadimran@gmail.com
 */

class UserRepository(private val client: HttpClient) {

    suspend fun getUserProfile(): HttpResponse = client.get(ProfileRequest.GetUserProfile)

    suspend fun updateUserProfile(body: Map<String, String>): HttpResponse =
        client.put(ProfileRequest.UserProfileRequest){
            setBody(body)
        }
}