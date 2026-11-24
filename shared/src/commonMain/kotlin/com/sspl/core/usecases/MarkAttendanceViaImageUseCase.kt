package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.AttendanceResponse
import com.sspl.core.models.ConferenceDetail
import com.sspl.core.repositories.ConferenceRepository
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class MarkAttendanceViaImageUseCase(private val conferenceRepository: ConferenceRepository) {
    operator fun invoke(
        conferenceId: Long, sessionId: Long, image: ByteArray
    ): Flow<ApiStates<AttendanceResponse>> = flow {
        emit(ApiStates.Loading)
        try {
            val response =
                conferenceRepository.markAttendanceViaImage(conferenceId, sessionId, image)

            if (response.status.isSuccess()) {
                val result = runCatching { response.body<AttendanceResponse>() }
                    .getOrElse { AttendanceResponse(status = "PENDING", createdAt = "", updatedAt = "") }
                emit(ApiStates.Success(result))
            } else {
                val errorText = runCatching { response.bodyAsText() }.getOrDefault("")
                val errorResponse = runCatching { response.body<ApiError>() }
                    .getOrElse { ApiError(message = errorText.ifBlank { "Failed to upload selfie." }) }

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
