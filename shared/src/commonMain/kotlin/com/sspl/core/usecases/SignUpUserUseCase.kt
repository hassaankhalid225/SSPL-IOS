package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.apis.AuthRequest
import com.sspl.core.models.ApiError
import com.sspl.core.models.AuthResponse
import com.sspl.core.models.User
import com.sspl.core.repositories.AuthRepository
import com.sspl.session.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SignUpUserUseCase(
    private val authRepository: AuthRepository,
    private val userSession: UserSession
) {

    fun signUpUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Flow<ApiStates<AuthResponse>> = flow {
        emit(ApiStates.Loading)
        try {
            val requestBody = mapOf(
                "first_name" to firstName,
                "last_name" to lastName,
                "email" to email,
                "password" to password,
                "phone_number" to phoneNumber
            )

            val response = authRepository.signUp(body = requestBody)

            if (response.status.isSuccess()) {
                val result = response.body<AuthResponse>()
                result.token?.let { userSession.setToken(result.token) }
                emit(ApiStates.Success(result))
            } else {
                val errorResponse = response.body<ApiError>()
                emit(ApiStates.Failure(error = errorResponse))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
        }
    }
}