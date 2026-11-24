package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.AttendanceResponse
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.repositories.ConferenceRepository
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class MarkAttendanceViaLocationUseCase(private val conferenceRepository: ConferenceRepository) {
    operator fun invoke(conferenceId: Long, sessionId: Long, lat: Double, lng: Double): Flow<ApiStates<AttendanceResponse>> =
        flow {
            emit(ApiStates.Loading)
            try {
                val response =
                    conferenceRepository.markAttendanceViaLocation(conferenceId, sessionId, lat, lng)

                if (response.status.isSuccess()) {
                    // Web treats any 2xx as marked (status defaults to ACCEPTED).
                    val result = runCatching { response.body<AttendanceResponse>() }
                        .getOrElse { AttendanceResponse(status = "ACCEPTED", createdAt = "", updatedAt = "") }
                    emit(ApiStates.Success(result))
                } else {
                    val errorText = runCatching { response.bodyAsText() }.getOrDefault("")
                    val errorResponse = runCatching { response.body<ApiError>() }
                        .getOrElse { ApiError(message = errorText.ifBlank { "Failed to mark attendance." }) }

                    val msg = (errorResponse.message ?: errorText).lowercase()
                    if (msg.contains("already marked") || msg.contains("already attended") || msg.contains("already recorded")) {
                        emit(ApiStates.Success(AttendanceResponse(status = "ACCEPTED", createdAt = "", updatedAt = "")))
                    } else {
                        emit(ApiStates.Failure(error = errorResponse))
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
                e.printStackTrace()
                emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
            }
        }
}
