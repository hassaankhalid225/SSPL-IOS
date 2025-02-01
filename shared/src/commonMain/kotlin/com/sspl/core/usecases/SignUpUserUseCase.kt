package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.apis.AuthRequest
import com.sspl.core.models.ApiError
import com.sspl.core.models.UserResponse
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
    private val authRepository: AuthRepository, private val userSession: UserSession
) {

    fun signUpUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Flow<ApiStates<UserResponse>> = flow {
        emit(ApiStates.Loading)
        try {
            val requestBody = mapOf(
                "first_name" to firstName.trim(),
                "last_name" to lastName.trim(),
                "gender" to "",
                "account" to mapOf(
                    "phone" to phoneNumber.trim(),
                    "email" to email.trim(),
                    "password" to password.trim()
                ),
                "profile" to mapOf(
                    "title" to "",
                    "overview" to "",
                    "pmdc_number" to "",
                    "org_number" to "",
                    "institute" to "",
                    "country" to "",
                    "contact" to mapOf(
                        "whatsapp" to "",
                        "twitter" to "",
                        "skype" to ""
                    )
                )
            )


            val response = authRepository.signUp(body = requestBody)

            if (response.status.isSuccess()) {
                val result = response.body<UserResponse>()
                result.accessToken?.let { userSession.setToken(it) }
               result?.user?.let { userSession.setUser(it) }

                println("the result is $result")
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