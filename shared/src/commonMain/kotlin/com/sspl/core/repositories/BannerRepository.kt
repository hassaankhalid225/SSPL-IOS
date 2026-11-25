package com.sspl.core.repositories

import com.sspl.core.apis.BannerRequest
import com.sspl.core.models.Banner
import com.sspl.core.models.BannersResponse
import com.sspl.utils.DateTimeUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.SYSTEM

class BannerRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    // In-memory cache + TTL (15 minutes)
    private var memoryCache: List<Banner>? = null
    private var lastFetchTimeMs: Long = 0L
    private val cacheTtlMs = 15 * 60 * 1000L

    // Disk cache (survives app restarts)
    private val diskDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "sspl_banners_cache"
    private val diskFile: Path = diskDir / "banners.json"

    /**
     * Synchronously returns cached banners (memory first, disk fallback) for instant UI painting.
     */
    fun cachedBanners(): List<Banner>? {
        if (memoryCache != null) return memoryCache
        return runCatching {
            if (!FileSystem.SYSTEM.exists(diskFile)) return@runCatching null
            val raw = FileSystem.SYSTEM.read(diskFile) { readUtf8() }
            val parsed = parseBanners(raw)
            if (parsed.isNotEmpty()) {
                memoryCache = parsed
                parsed
            } else null
        }.getOrNull()
    }

    private suspend fun writeDisk(raw: String) = withContext(Dispatchers.Default) {
        runCatching {
            FileSystem.SYSTEM.createDirectories(diskDir)
            val tmp = diskDir / "banners.json.tmp"
            FileSystem.SYSTEM.write(tmp) { writeUtf8(raw) }
            FileSystem.SYSTEM.atomicMove(tmp, diskFile)
        }
    }

    private fun parseBanners(jsonString: String): List<Banner> {
        return try {
            json.decodeFromString<BannersResponse>(jsonString).banners
        } catch (_: Exception) {
            try {
                json.decodeFromString<List<Banner>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Fetch all active banners with cache-first / stale-while-revalidate behaviour:
     * - Emits cached banners immediately if present.
     * - Skips network if cache is fresh (< 15 mins) and not forceRefresh.
     * - Revalidates in background when stale or forced, saving to disk and memory.
     */
    fun getBanners(forceRefresh: Boolean = false): Flow<BannersResponse> = flow {
        val now = DateTimeUtils.timeInMilliNow()
        val cached = cachedBanners()

        // 1. If fresh and not forced, return cached directly with zero network activity
        if (!forceRefresh && cached != null && (now - lastFetchTimeMs < cacheTtlMs)) {
            emit(BannersResponse(cached))
            return@flow
        }

        // 2. Instant UI paint if cached exists
        if (cached != null) {
            emit(BannersResponse(cached))
        }

        // 3. Network fetch / revalidation
        try {
            val jsonString = httpClient.get(BannerRequest()).body<String>()
            val banners = parseBanners(jsonString)
            if (banners.isNotEmpty() || cached == null) {
                memoryCache = banners
                lastFetchTimeMs = now
                writeDisk(jsonString)
                emit(BannersResponse(banners))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            println(">>> Banners: Network fetch failed: ${e.message}")
            if (cached == null) {
                emit(BannersResponse(emptyList()))
            }
        }
    }
}
