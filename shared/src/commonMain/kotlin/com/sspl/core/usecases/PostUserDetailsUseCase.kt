package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.AuthResponse
import com.sspl.core.models.ContactInfo
import com.sspl.core.models.UserProfile
import com.sspl.core.repositories.UserRepository
import com.sspl.session.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.isSuccess
 import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.ktor.client.call.*
import io.ktor.http.*

class PostUserDetailsUseCase(
    private val userRepository: UserRepository,
    private val userSession: UserSession
) {
    fun updateUserProfile(
        pmdcNo: String ="",
        instituteName: String ="",
        country: String ="",
        designation: String ="",
        registrationNo: String  ="",
        title: String ="",
        email: String ="",
        phone: String ="",
        whatsapp: String =""
    ): Flow<ApiStates<UserProfile>> = flow {
        emit(ApiStates.Loading)
        try {
            val requestBody = mapOf(
                "title" to title,
                "overview" to instituteName,
                "designation" to designation,
                "pmdc_number" to pmdcNo,
                "org_number" to registrationNo,
            )

            val response = userRepository.updateUserProfile(requestBody)
            if (response.status.isSuccess()) {
                println("the result is $response")
                val result = response.body<UserProfile>()
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
