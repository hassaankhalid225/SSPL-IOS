package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Resolves any image reference the backend returns into something Coil can load:
 *  - `data:image/...;base64,XXXX` → decoded ByteArray (the events API embeds images like this)
 *  - `http(s)://...`               → as is
 *  - `/uploads/x.png` or `uploads/x.png` → BASE_URL + path
 */
object ImageSources {
    private const val MAX_ENTRIES = 24
    private val cache = LinkedHashMap<Int, ByteArray>()

    fun isDataUri(src: String?) = src?.startsWith("data:") == true

    fun resolveUrl(src: String?): String? {
        val s = src?.trim()?.takeIf { it.isNotEmpty() && it != "null" } ?: return null
        return when {
            s.startsWith("http://") || s.startsWith("https://") -> s
            isDataUri(s) -> null
            s.startsWith("/") -> "$BASE_URL$s"
            else -> "$BASE_URL/$s"
        }
    }

    private val mutex = kotlinx.coroutines.sync.Mutex()

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun decodeDataUri(src: String): ByteArray? = withContext(Dispatchers.Default) {
        val key = src.hashCode() * 31 + src.length
        mutex.withLock { cache[key] }?.let { return@withContext it }
        val comma = src.indexOf(',')
        if (comma < 0) return@withContext null
        val bytes = runCatching { Base64.Mime.decode(src.substring(comma + 1)) }.getOrNull()
            ?: return@withContext null
        mutex.withLock {
            cache[key] = bytes
            while (cache.size > MAX_ENTRIES) cache.remove(cache.keys.first())
        }
        bytes
    }
}

/**
 * In-memory set of image URLs successfully loaded during the app process.
 * Used so that images don't re-shimmer or flash on every recomposition or scroll pass.
 */
object LoadedImageTracker {
    private const val MAX_URLS = 300
    private val loadedUrls = LinkedHashSet<String>()

    fun markLoaded(url: String?) {
        if (url.isNullOrBlank()) return
        if (loadedUrls.size >= MAX_URLS) {
            val first = loadedUrls.firstOrNull()
            if (first != null) loadedUrls.remove(first)
        }
        loadedUrls.add(url)
    }

    fun isLoaded(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return loadedUrls.contains(url)
    }
}

/**
 * The one image component screens should use.
 * Shows a skeleton while loading and a branded fallback if there's no image or it fails.
 */
@Composable
fun SsplImage(
    source: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: ImageVector = SsplIcons.Image,
    fallbackIconSize: Dp = 28.dp,
    fallbackBackground: Color? = null
) {
    val resolvedUrl = remember(source) { ImageSources.resolveUrl(source) }
    val model by produceState<Any?>(initialValue = resolvedUrl, source) {
        value = if (ImageSources.isDataUri(source)) ImageSources.decodeDataUri(source!!) else resolvedUrl
    }
    val initiallyReady = remember(source) {
        !source.isNullOrBlank() && (LoadedImageTracker.isLoaded(source) || (resolvedUrl != null && LoadedImageTracker.isLoaded(resolvedUrl)))
    }
    var state by remember(source) {
        mutableStateOf(
            when {
                source.isNullOrBlank() -> 2 // failed / empty
                initiallyReady -> 1 // already loaded in session! Instant paint.
                else -> 0 // loading
            }
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (model != null && state != 2) {
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onSuccess = {
                    state = 1
                    source?.let { LoadedImageTracker.markLoaded(it) }
                    resolvedUrl?.let { LoadedImageTracker.markLoaded(it) }
                },
                onError = {
                    state = 2
                }
            )
        }
        if (state == 0) {
            Box(Modifier.fillMaxSize().shimmer(androidx.compose.ui.graphics.RectangleShape))
        }
        if (state == 2 || (model == null && !ImageSources.isDataUri(source))) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        fallbackBackground?.let { androidx.compose.ui.graphics.SolidColor(it) }
                            ?: AppTheme.colors.headerGradient
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    fallbackIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(fallbackIconSize)
                )
            }
        }
    }
}
