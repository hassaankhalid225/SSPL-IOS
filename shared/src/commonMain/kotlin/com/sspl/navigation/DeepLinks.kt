package com.sspl.navigation

import io.ktor.http.decodeURLQueryComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A link the app knows how to open (web routes `/events/:eventId`, `/verify-certificate[/:serial]`). */
sealed interface DeepLink {
    data class EventDetail(val eventId: Long) : DeepLink
    data class VerifyCertificate(val serial: String?) : DeepLink
}

/**
 * Hand-off between the platform host (Android `MainActivity` intent data, iOS `onOpenURL`) and the
 * shared navigation. The host calls [handle]; `MainScreen` collects [pending], navigates, then [consume]s.
 * A link that arrives before the user reaches the main shell (splash / sign-in) stays pending until then.
 */
object DeepLinkBus {
    private val _pending = MutableStateFlow<DeepLink?>(null)
    val pending: StateFlow<DeepLink?> = _pending.asStateFlow()

    /** Parses [uri] and queues it. Returns false when the link isn't one the app handles. */
    fun handle(uri: String?): Boolean {
        val link = parse(uri) ?: return false
        _pending.value = link
        return true
    }

    fun consume(link: DeepLink) {
        if (_pending.value == link) _pending.value = null
    }

    /**
     * Supported:
     * - `sspl://events/{id}`, `https://<site>/events/{id}`
     * - `sspl://verify-certificate/{serial}`, `sspl://verify-certificate?serial=…`,
     *   `https://<site>/verify-certificate[/{serial}]`, `https://<site>/certificate/verify/{serial}`
     */
    fun parse(uri: String?): DeepLink? {
        val raw = uri?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val schemeEnd = raw.indexOf("://")
        if (schemeEnd <= 0) return null
        val scheme = raw.substring(0, schemeEnd).lowercase()
        val rest = raw.substring(schemeEnd + 3).substringBefore('#')
        val query = rest.substringAfter('?', "")
        val beforeQuery = rest.substringBefore('?')
        val host = beforeQuery.substringBefore('/').lowercase()
        val pathSegments = beforeQuery.substringAfter('/', "").split('/').filter { it.isNotBlank() }.map(::decode)
        // Custom scheme: host is the first route segment.
        val segments = if (scheme == "sspl") listOf(host) + pathSegments else pathSegments
        val params = query.split('&').filter { '=' in it }
            .associate { decode(it.substringBefore('=')) to decode(it.substringAfter('=')) }

        return when {
            segments.firstOrNull() == "events" -> {
                val id = segments.getOrNull(1)?.toLongOrNull() ?: params["id"]?.toLongOrNull() ?: return null
                DeepLink.EventDetail(id)
            }
            segments.firstOrNull() == "verify-certificate" ->
                DeepLink.VerifyCertificate(segments.getOrNull(1) ?: params["serial"]?.takeIf { it.isNotBlank() })
            segments.getOrNull(0) == "certificate" && segments.getOrNull(1) == "verify" ->
                DeepLink.VerifyCertificate(segments.getOrNull(2) ?: params["serial"]?.takeIf { it.isNotBlank() })
            else -> null
        }
    }

    private fun decode(s: String): String = runCatching {
        s.decodeURLQueryComponent(plusIsSpace = true)
    }.getOrDefault(s)
}
