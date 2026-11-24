package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.FeedbackForm
import com.sspl.core.repositories.ConferenceRepository
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

private val feedbackJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

class GetFeedbackFormUseCase(private val conferenceRepository: ConferenceRepository) {
    operator fun invoke(conferenceId: Long): Flow<ApiStates<FeedbackForm>> = flow {
        emit(ApiStates.Loading)
        try {
            val response = conferenceRepository.getFeedbackForm(conferenceId = conferenceId)
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                val result = runCatching { feedbackJson.decodeFromString<FeedbackForm>(text) }.getOrNull()
                emit(ApiStates.Success(result))
            } else {
                val errorText = runCatching { response.bodyAsText() }.getOrDefault("")
                val errorResponse = runCatching { feedbackJson.decodeFromString<ApiError>(errorText) }
                    .getOrElse { ApiError(message = errorText.ifBlank { "Failed to load feedback form." }) }
                emit(ApiStates.Failure(error = errorResponse))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
        }
    }
}