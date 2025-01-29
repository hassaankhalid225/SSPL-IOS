package com.sspl.core.usecases

import androidx.compose.ui.util.trace
import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.AuthResponse
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetPostDetailsUseCase(
       val httpClient: HttpClient   //demo -> to be replaced with some repo ..
) {

    operator fun invoke(id:Long) : Flow<ApiStates<Boolean>> = flow {
        emit(ApiStates.Loading)
        println(id.toString())
        delay(1500)
        emit(ApiStates.Success(true))
    }

}