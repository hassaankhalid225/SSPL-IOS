 package com.sspl.core
import com.sspl.session.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 11/01/2025.
 * se.muhammadimran@gmail.com
 */


 fun <T : HttpClientEngineConfig> getBaseClient(
    userSession: UserSession,
    engineFactory: HttpClientEngineFactory<T>,
    config: HttpClientConfig<T>.() -> Unit
): HttpClient {
    return HttpClient(engineFactory) {
        install(Resources)
//        install(HttpCache) {
//            publicStorage(CacheStorage.Disabled) // Disables response caching
//        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
        // Some list endpoints (e.g. /external/events) embed base64 images and weigh several MB.
        // Generous request timeout, tight connect timeout so offline fails fast.
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 60_000
            requestTimeoutMillis = 120_000
        }
        // Flaky mobile networks ("connection abort", DNS blips): retry idempotent GETs only.
        // POST/PUT/DELETE are never retried (no double registrations or payments).
        install(io.ktor.client.plugins.HttpRequestRetry) {
            maxRetries = 2
            retryOnExceptionIf { request, cause ->
                request.method == io.ktor.http.HttpMethod.Get &&
                    cause !is io.ktor.client.plugins.HttpRequestTimeoutException
            }
            retryIf { request, response ->
                request.method == io.ktor.http.HttpMethod.Get && response.status.value in 500..599
            }
            exponentialDelay()
        }
        // INFO = method + URL + status only. BODY logging stringified multi-MB payloads on every call
        // and was a major source of jank.
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        defaultRequest {
            url("${com.sspl.utils.BASE_URL}/")
            contentType(ContentType.Application.Json)
            val token = userSession.token
            token?.let {
                bearerAuth(it)
            }
            header(HttpHeaders.UserAgent, "SSPL Application")
        }

        config() // Apply additional configuration
    }.also { it.installTokenRefresh(userSession) } // silent 401 → /auth/refresh → replay
}
//}
//}
//        install(ContentNegotiation) {
//            json(Json {
//                ignoreUnknownKeys = true
//                prettyPrint = true
////                isLenient = true
//            })
//        }
//        install(Logging) {
//            logger = Logger.DEFAULT
//            level = LogLevel.ALL
//            filter { request ->
//                request.url.host.contains(HOST_URL)
//            }
//            sanitizeHeader { header -> header == HttpHeaders.Authorization }
//        }
//        defaultRequest {
//            url {
//                host = HOST_URL
//                port = 3000
//                protocol = URLProtocol.HTTP
//            } // Set to HTTP if needed
//            val token = ""
//            header(HttpHeaders.Authorization, "Bearer $token")
//            header(HttpHeaders.UserAgent, "SSPL Application")
//        }
//        config()
//    }
//}