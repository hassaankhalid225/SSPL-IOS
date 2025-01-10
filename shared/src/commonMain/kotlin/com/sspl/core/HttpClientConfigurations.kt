package com.sspl.core

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 11/01/2025.
 * se.muhammadimran@gmail.com
 */
private const val HOST_URL = "1.2.3.4"
private const val BASE_URL = "https://$HOST_URL"
private const val PATH_TO_APPEND = "/api"

fun <T : HttpClientEngineConfig> getBaseClient(
    engineFactory: HttpClientEngineFactory<T>,
    config: HttpClientConfig<T>.() -> Unit
): HttpClient {
    return HttpClient(engineFactory) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
//                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            filter { request ->
                request.url.host.contains(HOST_URL)
            }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        defaultRequest {
            host = HOST_URL
            port = 8080
            url { protocol = URLProtocol.HTTPS } // Set to HTTP if needed
            val token = ""
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.UserAgent, "SSPL Application")
        }
        config()
    }
}