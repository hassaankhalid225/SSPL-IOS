package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.CommonResponse
import com.sspl.core.models.FeedbackForm
import com.sspl.core.repositories.ConferenceRepository
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class PostFeedbackFormUseCase(private val conferenceRepository: ConferenceRepository) {
    /** [answers] = question/statement id -> value; body {data:[{question_id, value}]} like the web. */
    operator fun invoke(answers: Map<Int, String>, conferenceId: Long, sessionId: Long? = null): Flow<ApiStates<CommonResponse>> = flow {
        emit(ApiStates.Loading)
        try {
            val jsonBody = buildJsonObject {
                put("data", buildJsonArray {
                    answers.forEach { (id, value) ->
                        addJsonObject {
                            put("question_id", id)
                            put("value", value)
                        }
                    }
                })
            }

            val response = sessionId?.let {
                conferenceRepository.postFeedbackFormSession(
                    conferenceId = conferenceId,
                    sessionId=it,
                    body = jsonBody
                )
            }?:conferenceRepository.postFeedbackForm(conferenceId, body = jsonBody)
            if (response.status.isSuccess()) {
                // Success bodies vary; a missing `message` must not turn a saved answer into an error.
                val responseBody = runCatching { response.body<CommonResponse>() }
                    .getOrElse { CommonResponse(message = "Thanks for your feedback!") }
                 emit(ApiStates.Success(responseBody))
            } else {
                val errorResponse = runCatching { response.body<ApiError>() }
                    .getOrElse { ApiError(message = "Failed to submit feedback.") }
                emit(ApiStates.Failure(error = errorResponse))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
            e.printStackTrace()
            emit(ApiStates.Failure(ApiError(message = e.message.orEmpty())))
        }
    }
}