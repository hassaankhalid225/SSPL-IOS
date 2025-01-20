package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.models.AuthResponse
import com.sspl.core.models.ErrorResponse
import com.sspl.core.models.SignInRequest
import com.sspl.core.models.SignUpRequest
import com.sspl.core.models.User

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.preparePost
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class LoginUserUseCase(private val client: HttpClient) {

    fun signInUserViaEmail(
        email: String,
        password: String
    ): Flow<ApiStates<AuthResponse>> = flow {
        emit(ApiStates.Loading)

        try {
            val requestBody = mapOf(
                "email" to email,
                "password" to password,
            )

            val response = client.post(SignInRequest()) {
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val user = response.body<AuthResponse>()
                println(user.toString())
                emit(ApiStates.Success(user))
            } else {
                val errorResponse = response.body<ErrorResponse>()
                emit(ApiStates.Failure("Error: ${errorResponse.message}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiStates.Failure(e.message ?: "Unknown error occurred"))
        }
    }
}