package com.sspl.core.usecases


import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.Conference
import com.sspl.core.repositories.ConferenceRepository
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetConferencesDetailsUseCase(private val conferenceRepository: ConferenceRepository){
    fun getConferenceById(conferenceId: Int, format: String? = null): Flow<ApiStates<Conference>> = flow {
        emit(ApiStates.Loading)
        try {
            println(conferenceId.toString()+"is the id ")
            val response = conferenceRepository.getConferenceById(conferenceId)

            if (response.status.isSuccess()) {
                val result = response.body<Conference>()
                println("The result is $result")
                emit(ApiStates.Success(result))
            } else {
                println("new result is "+response.body())
                val errorResponse = response.body<ApiError>()
                println("The result is $errorResponse")

                emit(ApiStates.Failure(error = errorResponse))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
        }
    }}
