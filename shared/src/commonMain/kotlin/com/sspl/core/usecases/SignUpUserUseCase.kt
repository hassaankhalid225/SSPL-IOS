package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.UserResponse
import com.sspl.core.repositories.AuthRepository
import com.sspl.session.UserSession
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import com.sspl.core.push.PushNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SignUpUserUseCase(
    private val authRepository: AuthRepository,
    private val userSession: UserSession,
    private val pushNotificationService: PushNotificationService
) {

    fun signUpUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        gender: String = "MALE",
        prefix: String = "Mr",
        institute: String = "",
        city: String = "",
        country: String = ""
    ): Flow<ApiStates<UserResponse>> = flow {
        emit(ApiStates.Loading)
        try {
            // Same payload as web Signup.jsx (prefix defaults to "Mr", gender MALE/FEMALE/OTHER).
            // The password is sent exactly as typed — never trimmed.
            val body = buildJsonObject {
                put("first_name", firstName.trim())
                put("last_name", lastName.trim())
                put("prefix", prefix)
                put("gender", gender)
                put("account", buildJsonObject {
                    put("email", email.trim())
                    put("password", password)
                    put("phone", phoneNumber.trim())
                })
                put("profile", buildJsonObject {
                    put("institute", institute.trim())
                    put("city", city.trim())
                    put("country", country.trim())
                })
            }
            val response = authRepository.signUp(body = body)

            if (response.status.isSuccess()) {
                val result = response.body<UserResponse>()
                result.accessToken?.let { userSession.setToken(it) }
                result.refreshToken?.let { userSession.setRefreshToken(it) }
                result.user?.let { userSession.setUser(it) }
                
                // Register device token after successful signup
                pushNotificationService.registerDeviceToken()

                emit(ApiStates.Success(result))
            } else {
                val parsed = response.parseApiError()
                // Raw server text is kept so the VM can tell email vs phone conflicts (web parity).
                emit(ApiStates.Failure(error = ApiError(
                    message = parsed.rawMessage?.takeIf { it.isNotBlank() } ?: parsed.message,
                    error = "HTTP_${parsed.status}"
                )))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
        }
    }
}

