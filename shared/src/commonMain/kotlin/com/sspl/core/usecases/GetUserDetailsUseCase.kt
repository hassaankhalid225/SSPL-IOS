package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.ContactInfo
import com.sspl.core.models.UserProfile
import com.sspl.core.repositories.UserRepository
import com.sspl.session.UserSession
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetUserDetailsUseCase(
    private val userRepository: UserRepository,
 ) {
    fun getUserProfile(
    ): Flow<ApiStates<UserProfile>> = flow {
        emit(ApiStates.Loading)
        try {
            val response = userRepository.getUserProfile()
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