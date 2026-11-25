package com.sspl.core.repositories

import com.sspl.core.models.ContactInquiryRequest
import com.sspl.core.models.DownloadsResponse
import com.sspl.core.models.ElectionItem
import com.sspl.core.models.MembersDirectoryResponse
import com.sspl.core.models.PublicEventItem
import com.sspl.core.models.SiteSettingsResponse
import com.sspl.core.models.SocietyNewsItem
import com.sspl.utils.BASE_URL
import com.sspl.utils.DateTimeUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/** Failure with a message that is safe to show to the user. */
class PublicApiException(message: String) : Exception(message)

/**
 * Public / society information endpoints (same calls the web makes):
 * GET /society-news · GET /elections/ · GET /downloads · GET /certificates/verify/{serial}
 * GET /fellowship/certificate/verify/{serial} · GET /membership/statistics-and-list
 * GET /settings · POST /config/contact · GET /external/events?active=true
 *
 * The `fetch*` functions return [Result] so screens can tell "empty" apart from "failed".
 */
class PublicRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ------------------------------------------------------------------ helpers

    private suspend inline fun <T> runCatchingApi(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (c: CancellationException) {
            throw c
        } catch (e: PublicApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e // keep flow cancellation transparent
            println("PublicRepository error: ${e.message}")
            Result.failure(PublicApiException("We couldn't reach the server. Check your connection and try again."))
        }

    /** Reads the body; throws [PublicApiException] with the server's message for non-2xx responses. */
    private suspend fun HttpResponse.textOrThrow(fallback: String): String {
        val text = bodyAsText()
        if (!status.isSuccess()) throw PublicApiException(serverMessage(text) ?: fallback)
        return text
    }

    private fun serverMessage(text: String): String? = runCatching {
        val obj = json.parseToJsonElement(text) as? JsonObject ?: return null
        (obj["message"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: (obj["error"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun JsonObject.str(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull

    // ------------------------------------------------------------------ news

    // In-memory cache for news announcements
    private var newsCache: List<SocietyNewsItem>? = null
    private var newsLastFetchedMs: Long = 0L
    private val newsTtlMs = 15 * 60 * 1000L

    /** GET /society-news — active + visible items, newest first (same as web). */
    suspend fun fetchSocietyNews(forceRefresh: Boolean = false): Result<List<SocietyNewsItem>> {
        val now = DateTimeUtils.timeInMilliNow()
        if (!forceRefresh && newsCache != null && (now - newsLastFetchedMs < newsTtlMs)) {
            return Result.success(newsCache!!)
        }
        val result = runCatchingApi {
            val text = httpClient.get("$BASE_URL/society-news").textOrThrow("Failed to load announcements.")
            withContext(Dispatchers.Default) {
                val parsed = json.parseToJsonElement(text)
                val list: JsonArray? = when {
                    parsed is JsonArray -> parsed
                    parsed is JsonObject && parsed["data"] is JsonObject -> {
                        val data = parsed["data"]!!.jsonObject
                        (data["notifications"] ?: data["news"]) as? JsonArray
                    }
                    parsed is JsonObject && parsed["notifications"] is JsonArray -> parsed["notifications"]!!.jsonArray
                    else -> null
                }
                val items: List<SocietyNewsItem> = list?.let { json.decodeFromJsonElement(it) } ?: emptyList()
                items.filter { it.isActive && it.isVisible }.sortedByDescending { it.createdAt ?: "" }
            }
        }
        if (result.isSuccess) {
            newsCache = result.getOrNull()
            newsLastFetchedMs = now
        }
        return result
    }

    /** Flow wrapper kept for Home (emits an empty list on failure; Home hides the section). */
    fun getSocietyNews(forceRefresh: Boolean = false): Flow<List<SocietyNewsItem>> = flow {
        // Emit cached immediately if present (zero wait)
        newsCache?.let { emit(it) }
        val res = fetchSocietyNews(forceRefresh)
        if (res.isSuccess) {
            emit(res.getOrDefault(emptyList()))
        } else if (newsCache == null) {
            emit(emptyList())
        }
    }

    // ------------------------------------------------------------------ events

    /** GET /external/events?active=true */
    fun getUpcomingEvents(): Flow<List<PublicEventItem>> = flow {
        val result = runCatchingApi {
            val text = httpClient.get("$BASE_URL/external/events") { parameter("active", "true") }
                .textOrThrow("Failed to load events.")
            val parsed = json.parseToJsonElement(text)
            when {
                parsed is JsonArray -> json.decodeFromJsonElement<List<PublicEventItem>>(parsed)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                parsed is JsonObject && parsed["events"] is JsonArray -> json.decodeFromJsonElement(parsed["events"]!!.jsonArray)
                else -> emptyList()
            }
        }
        emit(result.getOrDefault(emptyList()))
    }

    // ------------------------------------------------------------------ elections

    /** Mirrors the web's normalizeElection: files[] + attachments[] + file_url + file → files[]. */
    private fun normalizeElectionJson(element: JsonElement): JsonElement {
        if (element !is JsonObject) return element
        val map = element.toMutableMap()
        val files = mutableListOf<JsonObject>()
        fun collect(arr: JsonElement?) {
            (arr as? JsonArray)?.forEach { item ->
                when (item) {
                    is JsonObject -> files.add(item)
                    is JsonPrimitive -> if (item !is JsonNull) files.add(JsonObject(mapOf("url" to item)))
                    else -> {}
                }
            }
        }
        collect(element["files"])
        collect(element["attachments"])
        element.str("file_url")?.takeIf { it.isNotBlank() }?.let { url ->
            files.add(
                JsonObject(
                    mapOf(
                        "url" to JsonPrimitive(url),
                        "name" to JsonPrimitive(element.str("file_name") ?: element.str("filename") ?: element.str("title") ?: "Attachment"),
                        "type" to JsonPrimitive(element.str("file_type") ?: element.str("mime_type") ?: "")
                    )
                )
            )
        }
        when (val f = element["file"]) {
            is JsonObject -> files.add(f)
            is JsonPrimitive -> if (f !is JsonNull) files.add(JsonObject(mapOf("url" to f)))
            else -> {}
        }
        map["files"] = JsonArray(files.distinctBy { it.str("url") }.filter { !it.str("url").isNullOrBlank() })
        map.remove("attachments")
        map.remove("file_url")
        return JsonObject(map)
    }

    /** GET /elections/ */
    suspend fun fetchElections(): Result<List<ElectionItem>> = runCatchingApi {
        val text = httpClient.get("$BASE_URL/elections/").textOrThrow("Failed to load elections. Please try again later.")
        withContext(Dispatchers.Default) {
            val parsed = json.parseToJsonElement(text)
            val raw: List<JsonElement> = when {
                parsed is JsonArray -> parsed
                parsed is JsonObject && parsed["records"] is JsonArray -> parsed["records"]!!.jsonArray
                parsed is JsonObject && parsed["elections"] is JsonArray -> parsed["elections"]!!.jsonArray
                parsed is JsonObject && parsed["data"] is JsonArray -> parsed["data"]!!.jsonArray
                else -> emptyList()
            }
            raw.map { json.decodeFromJsonElement<ElectionItem>(normalizeElectionJson(it)) }
        }
    }

    // ------------------------------------------------------------------ downloads

    /**
     * GET /downloads?category=&search=&page=&limit=
     * The API returns `format`, `original_filename` and `meta.totalPages`; they are mapped onto
     * the app model (`file_type`, `file_name`, `meta.total_pages`), which used different keys.
     */
    suspend fun fetchDownloads(
        category: String = "",
        search: String = "",
        page: Int = 1,
        limit: Int = 12
    ): Result<DownloadsResponse> = runCatchingApi {
        val text = httpClient.get("$BASE_URL/downloads") {
            if (category.isNotBlank()) parameter("category", category)
            if (search.isNotBlank()) parameter("search", search)
            parameter("page", page)
            parameter("limit", limit)
        }.textOrThrow("Failed to load downloads.")
        withContext(Dispatchers.Default) {
            val root = json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
            val items = (root["items"] as? JsonArray)?.map { el ->
                val o = el as? JsonObject ?: return@map el
                val m = o.toMutableMap()
                if (o["file_type"] == null && o["format"] != null) m["file_type"] = o["format"]!!
                if (o["file_name"] == null && o["original_filename"] != null) m["file_name"] = o["original_filename"]!!
                JsonObject(m)
            } ?: emptyList()
            val meta = (root["meta"] as? JsonObject)?.let { o ->
                val m = o.toMutableMap()
                if (o["total_pages"] == null && o["totalPages"] != null) m["total_pages"] = o["totalPages"]!!
                JsonObject(m)
            } ?: JsonObject(emptyMap())
            json.decodeFromJsonElement<DownloadsResponse>(
                JsonObject(mapOf("items" to JsonArray(items), "meta" to meta))
            )
        }
    }

    /** File endpoint used by the web for preview / download. */
    fun downloadFileUrl(id: Long, download: Boolean = true): String =
        "$BASE_URL/downloads/$id/file" + if (download) "?download=1" else ""

    // ------------------------------------------------------------------ certificates

    /**
     * GET /certificates/verify/{serial} or GET /fellowship/certificate/verify/{serial} (FELLOW- prefix).
     * Returns the raw JSON object (the two endpoints use different field names). A 404 with a JSON
     * body (`{"verified":false,"message":…}`) is a valid "not found" answer, not a failure.
     */
    suspend fun verifyCertificate(serial: String): Result<JsonObject> = runCatchingApi {
        val clean = serial.trim()
        val encoded = clean.encodeURLPathPart()
        val url = if (clean.uppercase().startsWith("FELLOW-")) {
            "$BASE_URL/fellowship/certificate/verify/$encoded"
        } else {
            "$BASE_URL/certificates/verify/$encoded"
        }
        val response = httpClient.get(url)
        val text = response.bodyAsText()
        val parsed = runCatching { json.parseToJsonElement(text) as? JsonObject }.getOrNull()
        when {
            parsed != null -> parsed
            response.status.isSuccess() -> JsonObject(emptyMap())
            else -> throw PublicApiException("Failed to verify certificate. Please try again.")
        }
    }

    // ------------------------------------------------------------------ members

    /**
     * GET /membership/statistics-and-list?page=&limit=&role=&search=
     * The PMDC number lives in `account.user.profiles[0].pmdc_number`; it is lifted onto `user`.
     */
    suspend fun fetchMembersDirectory(
        page: Int = 1,
        role: String? = null,
        search: String? = null,
        limit: Int = 10
    ): Result<MembersDirectoryResponse> = runCatchingApi {
        val text = httpClient.get("$BASE_URL/membership/statistics-and-list") {
            parameter("page", page)
            parameter("limit", limit)
            if (!role.isNullOrBlank() && role != "ALL") parameter("role", role)
            if (!search.isNullOrBlank()) parameter("search", search.trim())
        }.textOrThrow("Failed to load members from server.")
        withContext(Dispatchers.Default) {
            val root = json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
            val records = (root["records"] as? JsonArray)?.map { rec ->
                val r = rec as? JsonObject ?: return@map rec
                val account = r["account"] as? JsonObject ?: return@map r
                val user = account["user"] as? JsonObject ?: return@map r
                if (user["pmdc_number"] != null) return@map r
                val pmdc = ((user["profiles"] as? JsonArray)?.firstOrNull() as? JsonObject)?.get("pmdc_number")
                    ?: return@map r
                val newUser = JsonObject(user.toMutableMap().apply { put("pmdc_number", pmdc) })
                val newAccount = JsonObject(account.toMutableMap().apply { put("user", newUser) })
                JsonObject(r.toMutableMap().apply { put("account", newAccount) })
            } ?: emptyList()
            val fixed = JsonObject(root.toMutableMap().apply { put("records", JsonArray(records)) })
            json.decodeFromJsonElement<MembersDirectoryResponse>(fixed)
        }
    }

    // ------------------------------------------------------------------ settings / contact

    /** GET /settings — `contacts` block (phone, email, address, map). */
    suspend fun fetchSiteSettings(): Result<SiteSettingsResponse> = runCatchingApi {
        val text = httpClient.get("$BASE_URL/settings").textOrThrow("Failed to load contact information.")
        val result = json.decodeFromString<SiteSettingsResponse>(text)
        if (result.contacts == null) throw PublicApiException("Contact information is missing from settings.")
        result
    }

    /** POST /config/contact { name, email, message } — returns the server's confirmation text. */
    suspend fun submitContactInquiry(request: ContactInquiryRequest): Result<String?> = runCatchingApi {
        val response = httpClient.post("$BASE_URL/config/contact") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val text = response.textOrThrow("Failed to send message.")
        serverMessage(text)
    }
}
