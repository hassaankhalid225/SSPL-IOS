package com.sspl.core.usecases

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SignUpUserUseCase(private val client: HttpClient) {


    suspend fun signUpUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<String> {
        return try {
            val requestBody = mapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "email" to email,
                "password" to password,
                "phone_number" to phoneNumber
            )

            val response: HttpResponse = client.post("$1/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(requestBody))
            }
            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}