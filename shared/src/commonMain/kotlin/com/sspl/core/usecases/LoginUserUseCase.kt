package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.AuthResponse
import com.sspl.core.repositories.AuthRepository
import com.sspl.session.UserSession

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.ktor.client.call.*
import io.ktor.http.isSuccess

class LoginUserUseCase(private val authRepository: AuthRepository,private val userSession: UserSession) {

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

            val response = authRepository.login(body = requestBody)

            if (response.status.isSuccess()) {
                val user = response.body<AuthResponse>()
                println(user.toString())
                user.accessToken?.let { userSession.setToken(it) }

                emit(ApiStates.Success(user))
            } else {
                val errorResponse = response.body<ApiError>()
                emit(ApiStates.Failure(error = errorResponse))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message ?: "Unknown error occurred")))
        }
    }
}