package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.models.ErrorResponse
import com.sspl.core.models.SignUpRequest
import com.sspl.core.models.User
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.preparePost
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignUpUserUseCase(private val client: HttpClient) {

    fun signUpUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Flow<ApiStates<User>> = flow {
        emit(ApiStates.Loading)
        try {
            val requestBody = mapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "email" to email,
                "password" to password,
                "phone_number" to phoneNumber
            )

            val response = client.post(SignUpRequest()) {
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val user = response.body<User>()
                println(user.toString())
                emit(ApiStates.Success(user))
            } else {
                val errorResponse = response.body<ErrorResponse>()
                emit(ApiStates.Failure("Error: ${errorResponse.message}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiStates.Failure(e.message))
        }
    }
}