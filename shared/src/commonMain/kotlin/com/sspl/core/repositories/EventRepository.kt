package com.sspl.core.repositories

import com.sspl.core.models.EventArchiveRecord
import com.sspl.core.models.EventArchiveResponse
import com.sspl.core.models.EventCheckData
import com.sspl.core.models.EventCheckResponse
import com.sspl.core.models.EventItem
import com.sspl.core.models.EventPaymentInfo
import com.sspl.core.models.EventRegisterResponse
import com.sspl.core.models.EventRetryPaymentResponse
import com.sspl.core.models.SwichVerifyStatusRequest
import com.sspl.core.models.SwichVerifyStatusResponse
import com.sspl.utils.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@OptIn(ExperimentalTime::class)
class EventRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    // ---- In-memory cache -------------------------------------------------------------
    // /external/events embeds base64 images (several MB). Cache per (type, active) so tab
    // switches, Home and the Events tab share one download; dedupe concurrent requests.
    private data class CacheEntry(val items: List<EventItem>, val at: Long)
    private val cache = mutableMapOf<String, CacheEntry>()
    private val inFlight = mutableMapOf<String, Deferred<Result<List<EventItem>>>>()
    private val cacheMutex = Mutex()
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cacheTtlMs = 5 * 60 * 1000L

    // ---- Disk cache (survives app restarts) ---------------------------------------------
    // The feed is multi-MB and the server is slow (~20 KB/s on mobile), so the last good
    // response is kept on disk: launch shows it instantly and refreshes in the background;
    // if the network fails, the saved copy is shown instead of an error.
    private val diskDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "sspl_events_cache"

    private fun diskFile(key: String): Path =
        diskDir / ("events_" + key.replace(Regex("[^A-Za-z0-9]"), "_") + ".json")

    private suspend fun readDisk(key: String): List<EventItem>? = withContext(Dispatchers.Default) {
        runCatching {
            val f = diskFile(key)
            if (!FileSystem.SYSTEM.exists(f)) return@runCatching null
            parseEventList(FileSystem.SYSTEM.read(f) { readUtf8() })
        }.getOrNull()
    }

    private suspend fun writeDisk(key: String, raw: String) = withContext(Dispatchers.Default) {
        runCatching {
            FileSystem.SYSTEM.createDirectories(diskDir)
            val tmp = diskFile(key).parent!! / (diskFile(key).name + ".tmp")
            FileSystem.SYSTEM.write(tmp) { writeUtf8(raw) }
            FileSystem.SYSTEM.atomicMove(tmp, diskFile(key))
        }
    }

    private fun cacheKey(type: String?, active: Boolean?) =
        "${type?.takeIf { it.isNotBlank() && it != "ALL" } ?: "*"}|${active ?: "*"}"

    /** Cached events if any (instant first paint), else null. */
    fun cachedEvents(type: String? = null, active: Boolean? = true): List<EventItem>? =
        cache[cacheKey(type, active)]?.items

    /**
     * Load events with cache + request de-duplication. Parsing runs on Dispatchers.Default.
     * Returns a failure Result (instead of an empty list) so the UI can show a retry state.
     */
    suspend fun loadEvents(
        type: String? = null,
        active: Boolean? = true,
        forceRefresh: Boolean = false
    ): Result<List<EventItem>> {
        val key = cacheKey(type, active)
        val now = Clock.System.now().toEpochMilliseconds()
        // Cold start: serve the saved copy instantly, refresh in the background.
        if (!forceRefresh && cache[key] == null) {
            readDisk(key)?.let { saved ->
                cacheMutex.withLock { if (cache[key] == null) cache[key] = CacheEntry(saved, 0L) }
                repoScope.launch { startFetch(type, active, key).await() }
                return Result.success(saved)
            }
        }
        val deferred = cacheMutex.withLock {
            val hit = cache[key]
            if (!forceRefresh && hit != null && now - hit.at < cacheTtlMs) {
                return Result.success(hit.items)
            }
            null
        } ?: startFetch(type, active, key)
        val result = deferred.await()
        if (result.isSuccess) return result
        // Network failed: fall back to whatever we have (memory, then disk).
        val fallback = cache[key]?.items ?: readDisk(key)
        return if (fallback != null) Result.success(fallback) else result
    }

    /** Starts (or joins) the network fetch for [key]; updates memory + disk on success. */
    private suspend fun startFetch(type: String?, active: Boolean?, key: String): Deferred<Result<List<EventItem>>> =
        cacheMutex.withLock {
            inFlight[key] ?: repoScope.async {
                val r = fetchEvents(type, active, key)
                cacheMutex.withLock {
                    inFlight.remove(key)
                    r.getOrNull()?.let { cache[key] = CacheEntry(it, Clock.System.now().toEpochMilliseconds()) }
                }
                r
            }.also { inFlight[key] = it }
        }

    private suspend fun fetchEvents(type: String?, active: Boolean?, key: String): Result<List<EventItem>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val responseText = httpClient.get("$BASE_URL/external/events") {
                    if (!type.isNullOrBlank() && type != "ALL") parameter("type", type)
                    if (active != null) parameter("active", active.toString())
                }.body<String>()
                parseEventList(responseText).also { writeDisk(key, responseText) }
            }.onFailure { println("EventRepository.loadEvents error: ${it.message}") }
        }

    private fun parseEventList(responseText: String): List<EventItem> {
        val parsed = json.parseToJsonElement(responseText)
        return when {
            parsed is JsonArray -> json.decodeFromJsonElement(parsed)
            parsed is JsonObject && parsed["data"] is JsonArray ->
                json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
            parsed is JsonObject && parsed["data"] is JsonObject && parsed["data"]!!.jsonObject["events"] is JsonArray ->
                json.decodeFromJsonElement(parsed["data"]!!.jsonObject["events"]!!.jsonArray)
            parsed is JsonObject && parsed["events"] is JsonArray ->
                json.decodeFromJsonElement(parsed["events"]!!.jsonArray)
            else -> emptyList()
        }
    }

    /**
     * Fetch events from GET /external/events (legacy Flow API — now cache-backed).
     * Emits an empty list on failure; prefer [loadEvents] for error-aware callers.
     */
    fun getEvents(type: String? = null, active: Boolean? = true): Flow<List<EventItem>> = flow {
        emit(loadEvents(type, active).getOrDefault(emptyList()))
    }

    /**
     * Events open for abstract submission (monthly meetings, conferences, programmes).
     * GET /external/events?abstract_eligible=true&active=true
     */
    fun getAbstractEligibleEvents(): Flow<List<EventItem>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/external/events") {
                parameter("abstract_eligible", "true")
                parameter("active", "true")
            }.body<String>()

            val parsed = json.parseToJsonElement(responseText)
            val eventsList: List<EventItem> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                parsed is JsonObject && parsed["data"] is JsonObject && parsed["data"]!!.jsonObject["events"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonObject["events"]!!.jsonArray)
                parsed is JsonObject && parsed["events"] is JsonArray -> json.decodeFromJsonElement(parsed["events"]!!.jsonArray)
                else -> emptyList()
            }
            emit(eventsList)
        } catch (e: Exception) {
            println("EventRepository.getAbstractEligibleEvents error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Fetch single event details from GET /external/events/{id}
     */
    fun getEventById(id: Long): Flow<EventItem?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/external/events/$id").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val item: EventItem? = when {
                parsed is JsonObject && parsed["data"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["data"]!!)
                }
                parsed is JsonObject && parsed["event"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["event"]!!)
                }
                parsed is JsonObject -> {
                    json.decodeFromJsonElement(parsed)
                }
                else -> null
            }
            emit(item)
        } catch (e: Exception) {
            println("EventRepository.getEventById error: ${e.message}")
            emit(null)
        }
    }

    /**
     * Check current user's registration status for an event from GET /external/events/{id}/check
     */
    fun checkEventStatus(eventId: Long): Flow<EventCheckData?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/external/events/$eventId/check").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val raw: JsonObject? = when {
                parsed is JsonObject && parsed["data"] is JsonObject -> parsed["data"]!!.jsonObject
                parsed is JsonObject -> parsed
                else -> null
            }
            // Same registration normalisation as my-registrations (nested conference/role tolerance).
            val checkData: EventCheckData? = raw?.let { obj ->
                val m = obj.toMutableMap()
                (m["registration"] as? JsonObject)?.let { m["registration"] = normaliseRegistration(it) }
                json.decodeFromJsonElement<EventCheckData>(JsonObject(m))
            }
            emit(checkData)
        } catch (e: Exception) {
            println("EventRepository.checkEventStatus error: ${e.message}")
            emit(null)
        }
    }

    /**
     * Register current user for an event from POST /external/events/{id}/register
     */
    fun registerForEvent(eventId: Long, roleId: Long?): Flow<EventRegisterResponse> = flow {
        try {
            val payload = buildMap<String, Any?> {
                if (roleId != null && roleId > 0) {
                    put("role_id", roleId)
                }
            }
            val responseText = httpClient.post("$BASE_URL/external/events/$eventId/register") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }.body<String>()

            val parsed = json.decodeFromString<EventRegisterResponse>(responseText)
            emit(parsed)
        } catch (e: Exception) {
            println("EventRepository.registerForEvent error: ${e.message}")
            emit(EventRegisterResponse(success = false, message = e.message ?: "Failed to register for event"))
        }
    }

    /**
     * Retry payment for an event registration from POST /external/events/registrations/{id}/pay
     */
    fun retryEventPayment(registrationId: Long): Flow<EventRetryPaymentResponse> = flow {
        try {
            val responseText = httpClient.post("$BASE_URL/external/events/registrations/$registrationId/pay") {
                contentType(ContentType.Application.Json)
                setBody(emptyMap<String, String>())
            }.body<String>()

            val parsed = json.decodeFromString<EventRetryPaymentResponse>(responseText)
            emit(parsed)
        } catch (e: Exception) {
            println("EventRepository.retryEventPayment error: ${e.message}")
            emit(EventRetryPaymentResponse(success = false, message = e.message ?: "Payment retry failed"))
        }
    }

    /**
     * Verify Swich payment transaction status from POST /swich/verify-status
     */
    fun verifySwichPayment(customerTransactionId: String): Flow<SwichVerifyStatusResponse> = flow {
        try {
            val responseText = httpClient.post("$BASE_URL/swich/verify-status") {
                contentType(ContentType.Application.Json)
                setBody(SwichVerifyStatusRequest(customerTransactionId = customerTransactionId))
            }.body<String>()

            val parsed = json.decodeFromString<SwichVerifyStatusResponse>(responseText)
            emit(parsed)
        } catch (e: Exception) {
            println("EventRepository.verifySwichPayment error: ${e.message}")
            emit(SwichVerifyStatusResponse(success = false, status = "failed", message = e.message ?: "Verification failed"))
        }
    }

    /**
     * Fetch public event archives from GET /archive/public
     */
    fun getPublicArchives(): Flow<List<EventArchiveRecord>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/archive/public").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val archives: List<EventArchiveRecord> = when {
                parsed is JsonArray -> {
                    json.decodeFromJsonElement(parsed)
                }
                parsed is JsonObject && parsed["data"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(archives)
        } catch (e: Exception) {
            println("EventRepository.getPublicArchives error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Error-aware variant of [getPublicArchives] (GET /archive/public) so the UI can show retry.
     */
    suspend fun loadPublicArchives(): Result<List<EventArchiveRecord>> = withContext(Dispatchers.Default) {
        runCatching {
            val responseText = httpClient.get("$BASE_URL/archive/public").body<String>()
            when (val parsed = json.parseToJsonElement(responseText)) {
                is JsonArray -> json.decodeFromJsonElement<List<EventArchiveRecord>>(parsed)
                is JsonObject -> (parsed["data"] as? JsonArray)
                    ?.let { json.decodeFromJsonElement<List<EventArchiveRecord>>(it) }
                    ?: emptyList()
                else -> emptyList()
            }
        }
    }

    /**
     * Error-aware variant of [getMyRegistrations] (GET /external/events/my-registrations).
     * Each registration's nested `conference` object is normalised before decoding: the legacy
     * [com.sspl.core.models.Conference] model has non-default fields and a 3-value enum, so
     * one MONTHLY_MEETING / CONFERENCE row (or a missing `created_at`) used to fail the whole
     * payload, which the old code silently turned into "no registrations".
     */
    suspend fun loadMyRegistrations(): Result<com.sspl.core.models.MyRegistrationsData> =
        withContext(Dispatchers.Default) {
            runCatching {
                val responseText = httpClient.get("$BASE_URL/external/events/my-registrations").body<String>()
                val parsed = json.parseToJsonElement(responseText)
                val dataObj = (parsed as? JsonObject)?.let { it["data"] as? JsonObject ?: it }
                    ?: error("Unexpected response from server")
                json.decodeFromJsonElement<com.sspl.core.models.MyRegistrationsData>(normaliseRegistrations(dataObj))
            }
        }

    private fun normaliseRegistrations(data: JsonObject): JsonObject = JsonObject(
        data.mapValues { (_, v) ->
            when (v) {
                is JsonArray -> JsonArray(v.map { (it as? JsonObject)?.let(::normaliseRegistration) ?: it })
                is JsonObject -> if (v.containsKey("payment_status") || v.containsKey("fee_amount")) normaliseRegistration(v) else v
                else -> v
            }
        }
    )

    private fun normaliseRegistration(reg: JsonObject): JsonObject {
        val m = reg.toMutableMap()
        if (m["fee_amount"] == null || m["fee_amount"] is kotlinx.serialization.json.JsonNull) m["fee_amount"] = kotlinx.serialization.json.JsonPrimitive(0.0)
        if (m["payment_status"] == null || m["payment_status"] is kotlinx.serialization.json.JsonNull) m["payment_status"] = kotlinx.serialization.json.JsonPrimitive("PENDING")
        val conf = m["conference"] as? JsonObject
        if (conf != null) {
            val c = conf.toMutableMap()
            // Models now default every field; only strip values whose JSON *kind* is wrong.
            if (c["venue"] != null && c["venue"] !is JsonObject) c.remove("venue")
            if (c["id"] is kotlinx.serialization.json.JsonNull) c.remove("id")
            m["conference"] = JsonObject(c)
        }
        if (m["role"] != null && m["role"] !is JsonObject) m.remove("role")
        return JsonObject(m)
    }

    /**
     * D4: Fetch all registrations of current user from GET /external/events/my-registrations
     */
    fun getMyRegistrations(): Flow<com.sspl.core.models.MyRegistrationsData?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/external/events/my-registrations").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val data: com.sspl.core.models.MyRegistrationsData? = when {
                parsed is JsonObject && parsed["data"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["data"]!!)
                }
                parsed is JsonObject -> {
                    json.decodeFromJsonElement(parsed)
                }
                else -> null
            }
            emit(data)
        } catch (e: Exception) {
            println("EventRepository.getMyRegistrations error: ${e.message}")
            emit(null)
        }
    }
}

