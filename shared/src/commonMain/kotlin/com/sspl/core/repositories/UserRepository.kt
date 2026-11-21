package com.sspl.core.repositories

import com.sspl.core.apis.ProfileRequest
import com.sspl.core.models.Profile
import com.sspl.core.models.User
import com.sspl.core.requestbodies.UpdateDeviceTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonObject

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 21/01/2025.
 * se.muhammadimran@gmail.com
 */

class UserRepository(private val client: HttpClient) {

    suspend fun getUserProfile(): HttpResponse = client.get(ProfileRequest.GetUserProfile)

    suspend fun updateUserProfile(body: User): HttpResponse =
        client.put(ProfileRequest.UserProfileRequest) {
            setBody(body)
        }

    /** PUT /profiles/me with a JSON body built like web ProfilePage.jsx `handleSave`. */
    suspend fun updateUserProfile(body: JsonObject): HttpResponse =
        client.put(ProfileRequest.UserProfileRequest) {
            setBody(body)
        }

    suspend fun updateDeviceToken(deviceToken: String, devicePlatform: String): HttpResponse =
        client.put(ProfileRequest.UpdateDeviceToken) {
            setBody(UpdateDeviceTokenRequest(deviceToken, devicePlatform))
        }

    /**
     * PUT /profiles/me as multipart — same call the web makes in `services/userApi.js#updateUserProfile`
     * when a profile picture is attached: person fields as plain parts + file part `profile_picture`.
     * We only send first/last name (non-empty, like the web) alongside the image so nothing else changes.
     */
    suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        firstName: String?,
        lastName: String?,
        fileName: String = "profile.jpg"
    ): HttpResponse =
        client.put(ProfileRequest.UserProfileRequest) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        if (!firstName.isNullOrBlank()) append("first_name", firstName)
                        if (!lastName.isNullOrBlank()) append("last_name", lastName)
                        append("profile_picture", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                )
            )
        }
}
