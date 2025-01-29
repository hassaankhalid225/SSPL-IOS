package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.AuthResponse
import com.sspl.core.repositories.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PostUserDetailsUseCase (
    private val userRepository: UserRepository,
) {

    fun postUserDetails(
        pmdcNo: String?, instituteName: String, country: String, designation: String?, registrationNo: String
    ): Flow<ApiStates<Boolean>> = flow {
        emit(ApiStates.Loading)
        try {
            val requestBody = mapOf(
                "pmdcNo" to pmdcNo,
                "instituteName" to instituteName,
                "country" to country,
                "designation" to designation,
                "registrationNo" to registrationNo
            )

           // val response = userRepository.postPersonalInfo(body = requestBody)

//            if (response.status.isSuccess()) {
//                val result = response.body<AuthResponse>()
//                println("the result is $result")
//                emit(ApiStates.Success(result))
//            } else {
//                val errorResponse = response.body<ApiError>()
//                emit(ApiStates.Failure(error = errorResponse))
//            }
            emit(ApiStates.Success(true))

        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
        }
    }
}