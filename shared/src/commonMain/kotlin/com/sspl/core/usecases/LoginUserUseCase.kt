package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.models.Account
import com.sspl.core.models.ApiError
import com.sspl.core.models.UserResponse
import com.sspl.core.repositories.AuthRepository
import com.sspl.session.UserSession
import com.sspl.core.push.PushNotificationService

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.ktor.client.call.*
import io.ktor.http.isSuccess

class LoginUserUseCase(
    private val authRepository: AuthRepository,
    private val userSession: UserSession,
    private val pushNotificationService: PushNotificationService
) {

    fun signInUserViaEmail(
        email: String, password: String
    ): Flow<ApiStates<UserResponse>> = flow {
        emit(ApiStates.Loading)

        try {

            val requestBody = Account(
                email = email,
                password = password
            )
            val response = authRepository.login(body = requestBody)

            if (response.status.isSuccess()) {
                val authResponse = response.body<UserResponse>()
                println(authResponse.toString())
                authResponse.accessToken?.let { userSession.setToken(it) }
                authResponse.refreshToken?.let { userSession.setRefreshToken(it) }
                authResponse.user?.let { userSession.setUser(it) }
                
                pushNotificationService.registerDeviceToken()

                emit(ApiStates.Success(authResponse))
            } else {
                val errorResponse = response.body<ApiError>()
                emit(ApiStates.Failure(error = errorResponse))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message ?: "Unknown error occurred")))
        }
    }
}