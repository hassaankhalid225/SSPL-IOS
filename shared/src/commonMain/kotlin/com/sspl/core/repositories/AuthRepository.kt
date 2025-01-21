package com.sspl.core.repositories

import com.sspl.core.apis.AuthRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 21/01/2025.
 * se.muhammadimran@gmail.com
 */

class AuthRepository(private val client: HttpClient) {
    suspend fun login(body: Map<String, String>): HttpResponse =
        client.post(AuthRequest.Login()) {
            setBody(body)
        }

    suspend fun signUp(body: Map<String, String>): HttpResponse =
        client.post(AuthRequest.SignUp()) {
            setBody(body)
        }
}