package com.sspl.core

import com.sspl.session.UserSession
import com.sspl.utils.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.Sender
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Silent session renewal — mirrors the web's apiClient.js.
 *
 * Access tokens live 15 minutes and refresh tokens ROTATE on every use, so:
 *  - any 401 on an authenticated call triggers ONE POST /auth/refresh (single-flight via mutex),
 *    the new access + refresh tokens are saved, and the original request is replayed;
 *  - concurrent 401s wait for that refresh and reuse its token (a second refresh would use an
 *    already-rotated token and fail);
 *  - refresh rejected by the server (4xx) → session cleared + [UserSession.sessionExpired] fired
 *    (the app routes to sign-in once);
 *  - refresh failed for network reasons → session kept, original 401 returned (no logout offline).
 */
internal fun HttpClient.installTokenRefresh(userSession: UserSession) {
    val mutex = Mutex()
    val json = Json { ignoreUnknownKeys = true }

    plugin(HttpSend).intercept { request ->
        val sentAuth = request.headers[HttpHeaders.Authorization]
        val call = execute(request)
        val path = request.url.buildString().substringBefore('?')
        val isAuthEndpoint = path.endsWith("/auth/login") || path.endsWith("/auth/refresh") ||
            path.endsWith("/auth/signup") || path.endsWith("/login") || path.endsWith("/refresh")
        if (call.response.status != HttpStatusCode.Unauthorized || sentAuth.isNullOrBlank() || isAuthEndpoint) {
            return@intercept call
        }
        val sentToken = sentAuth.removePrefix("Bearer ").trim()

        val newToken: String? = mutex.withLock {
            val current = userSession.token
            if (!current.isNullOrBlank() && current != sentToken) {
                current // another request already refreshed while we waited
            } else {
                refreshTokens(userSession, json)
            }
        }
        if (newToken.isNullOrBlank()) return@intercept call

        request.headers.remove(HttpHeaders.Authorization)
        request.headers.append(HttpHeaders.Authorization, "Bearer $newToken")
        execute(request)
    }
}

/** Returns the new access token, or null. Clears the session only when the server rejects it. */
private suspend fun Sender.refreshTokens(userSession: UserSession, json: Json): String? {
    val refresh = userSession.refreshToken?.takeIf { it.isNotBlank() } ?: run {
        expire(userSession)
        return null
    }
    val req = HttpRequestBuilder().apply {
        url("$BASE_URL/auth/refresh")
        method = HttpMethod.Post
        headers.append(HttpHeaders.UserAgent, "SSPL Application")
        setBody(TextContent("{\"refresh_token\":${JsonPrimitive(refresh)}}", ContentType.Application.Json))
    }
    return try {
        val resp = execute(req).response
        when {
            resp.status.value in 200..299 -> {
                val obj = json.parseToJsonElement(resp.bodyAsText()).jsonObject
                val access = obj["access_token"]?.jsonPrimitive?.content
                if (access.isNullOrBlank()) {
                    null
                } else {
                    userSession.setToken(access)
                    obj["refresh_token"]?.jsonPrimitive?.content
                        ?.takeIf { it.isNotBlank() }
                        ?.let { userSession.setRefreshToken(it) }
                    access
                }
            }
            resp.status.value in 400..499 -> {
                expire(userSession)
                null
            }
            else -> null // 5xx: keep the session, try again next time
        }
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        null // offline / timeout: keep the session
    }
}

private suspend fun expire(userSession: UserSession) {
    userSession.resetUser()
    userSession.notifySessionExpired()
}
