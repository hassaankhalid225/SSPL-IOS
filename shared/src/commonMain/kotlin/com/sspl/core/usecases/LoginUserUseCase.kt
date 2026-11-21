package com.sspl.core.usecases


import com.sspl.core.ApiStates
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
        email: String, password: String, rememberMe: Boolean = false
    ): Flow<ApiStates<UserResponse>> = flow {
        emit(ApiStates.Loading)

        try {

            // Password is sent exactly as typed (never trimmed), like the web Login.jsx.
            val response = authRepository.login(email = email, password = password, rememberMe = rememberMe)

            if (response.status.isSuccess()) {
                val authResponse = response.body<UserResponse>()
                authResponse.accessToken?.let { userSession.setToken(it) }
                authResponse.refreshToken?.let { userSession.setRefreshToken(it) }
                authResponse.user?.let { userSession.setUser(it) }
                
                pushNotificationService.registerDeviceToken()

                emit(ApiStates.Success(authResponse))
            } else {
                // Friendly web message + "HTTP_<status>" so the VM can classify 404/401 like Login.jsx.
                val raw = response.parseApiError()
                val friendly = raw.toApiError()
                emit(ApiStates.Failure(error = if (raw.rawMessage?.lowercase()?.contains("not registered") == true)
                    friendly.copy(message = raw.rawMessage) else friendly))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message ?: "Unknown error occurred")))
        }
    }
}