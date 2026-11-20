package com.sspl.core.repositories

import com.sspl.core.models.CertificateVerifyResponse
import com.sspl.core.models.ContactInquiryRequest
import com.sspl.core.models.ContactInquiryResponse
import com.sspl.core.models.DownloadsResponse
import com.sspl.core.models.ElectionAttachment
import com.sspl.core.models.ElectionItem
import com.sspl.core.models.MembersDirectoryResponse
import com.sspl.core.models.PublicEventItem
import com.sspl.core.models.SiteSettingsResponse
import com.sspl.core.models.SocietyNewsItem
import com.sspl.utils.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class PublicRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Fetch society news announcements from GET /society-news
     */
    fun getSocietyNews(): Flow<List<SocietyNewsItem>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/society-news").body<String>()
            val parsedElement = json.parseToJsonElement(responseText)
            val items: List<SocietyNewsItem> = when {
                parsedElement is JsonArray -> {
                    json.decodeFromJsonElement(parsedElement)
                }
                parsedElement is JsonObject && parsedElement["data"] is JsonObject -> {
                    val dataObj = parsedElement["data"]!!.jsonObject
                    val listElement = dataObj["notifications"] ?: dataObj["news"]
                    if (listElement is JsonArray) json.decodeFromJsonElement(listElement) else emptyList()
                }
                parsedElement is JsonObject && parsedElement["notifications"] is JsonArray -> {
                    json.decodeFromJsonElement(parsedElement["notifications"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(items.filter { it.isActive && it.isVisible })
        } catch (e: Exception) {
            println("PublicRepository.getSocietyNews error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Fetch active upcoming events from GET /external/events?active=true
     */
    fun getUpcomingEvents(): Flow<List<PublicEventItem>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/external/events") {
                parameter("active", "true")
            }.body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val events: List<PublicEventItem> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                parsed is JsonObject && parsed["events"] is JsonArray -> json.decodeFromJsonElement(parsed["events"]!!.jsonArray)
                else -> emptyList()
            }
            emit(events)
        } catch (e: Exception) {
            println("PublicRepository.getUpcomingEvents error: ${e.message}")
            emit(emptyList())
        }
    }

    private fun normalizeElectionJson(element: JsonElement): JsonElement {
        if (element !is JsonObject) return element
        val map = element.toMutableMap()
        val filesList = mutableListOf<JsonObject>()

        element["files"]?.let { f ->
            if (f is JsonArray) {
                f.forEach { item ->
                    when (item) {
                        is JsonObject -> filesList.add(item)
                        is JsonPrimitive -> filesList.add(JsonObject(mapOf("url" to item)))
                        else -> {}
                    }
                }
            }
        }
        element["attachments"]?.let { a ->
            if (a is JsonArray) {
                a.forEach { item ->
                    when (item) {
                        is JsonObject -> filesList.add(item)
                        is JsonPrimitive -> filesList.add(JsonObject(mapOf("url" to item)))
                        else -> {}
                    }
                }
            }
        }
        element["file_url"]?.let { u ->
            if (u is JsonPrimitive && u.content.isNotBlank()) {
                filesList.add(JsonObject(mapOf(
                    "url" to u,
                    "name" to (element["file_name"] ?: element["title"] ?: JsonPrimitive("Attachment")),
                    "type" to (element["file_type"] ?: JsonPrimitive("application/pdf"))
                )))
            }
        }
        if (filesList.isNotEmpty()) {
            map["files"] = JsonArray(filesList)
        }
        return JsonObject(map)
    }

    /**
     * Fetch active elections from GET /elections/
     */
    fun getElections(): Flow<List<ElectionItem>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/elections/").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val rawList: List<JsonElement> = when {
                parsed is JsonArray -> parsed
                parsed is JsonObject && parsed["records"] is JsonArray -> parsed["records"]!!.jsonArray
                parsed is JsonObject && parsed["elections"] is JsonArray -> parsed["elections"]!!.jsonArray
                parsed is JsonObject && parsed["data"] is JsonArray -> parsed["data"]!!.jsonArray
                else -> emptyList()
            }
            val list: List<ElectionItem> = rawList.map { el ->
                json.decodeFromJsonElement<ElectionItem>(normalizeElectionJson(el))
            }
            emit(list)
        } catch (e: Exception) {
            println("PublicRepository.getElections error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Fetch single election by ID from GET /elections/{id}
     */
    fun getElectionById(id: Long): Flow<ElectionItem?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/elections/$id").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val rawElement: JsonElement? = when {
                parsed is JsonObject && parsed["data"] is JsonObject -> parsed["data"]
                parsed is JsonObject && parsed["election"] is JsonObject -> parsed["election"]
                parsed is JsonObject -> parsed
                else -> null
            }
            val item: ElectionItem? = rawElement?.let { el ->
                json.decodeFromJsonElement<ElectionItem>(normalizeElectionJson(el))
            }
            emit(item)
        } catch (e: Exception) {
            println("PublicRepository.getElectionById error: ${e.message}")
            emit(null)
        }
    }

    /**
     * Fetch public downloads from GET /downloads
     */
    fun getDownloads(
        category: String = "",
        search: String = "",
        page: Int = 1,
        limit: Int = 12
    ): Flow<DownloadsResponse> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/downloads") {
                if (category.isNotBlank()) parameter("category", category)
                if (search.isNotBlank()) parameter("search", search)
                parameter("page", page)
                parameter("limit", limit)
            }.body<String>()
            val result = json.decodeFromString<DownloadsResponse>(responseText)
            emit(result)
        } catch (e: Exception) {
            println("PublicRepository.getDownloads error: ${e.message}")
            emit(DownloadsResponse())
        }
    }

    /**
     * Verify certificate from GET /certificates/verify/{serial}
     * or GET /fellowship/certificate/verify/{serial}
     */
    fun verifyCertificate(serial: String): Flow<CertificateVerifyResponse> = flow {
        try {
            val cleanSerial = serial.trim()
            val url = if (cleanSerial.uppercase().startsWith("FELLOW-")) {
                "$BASE_URL/fellowship/certificate/verify/$cleanSerial"
            } else {
                "$BASE_URL/certificates/verify/$cleanSerial"
            }
            val responseText = httpClient.get(url).body<String>()
            val result = json.decodeFromString<CertificateVerifyResponse>(responseText)
            emit(result)
        } catch (e: Exception) {
            println("PublicRepository.verifyCertificate error: ${e.message}")
            emit(CertificateVerifyResponse(verified = false, message = e.message ?: "Verification failed"))
        }
    }

    /**
     * Fetch members directory from GET /membership/statistics-and-list
     */
    fun getMembersDirectory(
        page: Int = 1,
        role: String? = null,
        search: String? = null,
        limit: Int = 15
    ): Flow<MembersDirectoryResponse> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/membership/statistics-and-list") {
                parameter("page", page)
                parameter("limit", limit)
                if (!role.isNullOrBlank() && role != "ALL") {
                    parameter("role", role)
                }
                if (!search.isNullOrBlank()) {
                    parameter("search", search.trim())
                }
            }.body<String>()
            val result = json.decodeFromString<MembersDirectoryResponse>(responseText)
            emit(result)
        } catch (e: Exception) {
            println("PublicRepository.getMembersDirectory error: ${e.message}")
            emit(MembersDirectoryResponse())
        }
    }

    /**
     * Fetch site settings from GET /settings
     */
    fun getSiteSettings(): Flow<SiteSettingsResponse?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/settings").body<String>()
            val result = json.decodeFromString<SiteSettingsResponse>(responseText)
            emit(result)
        } catch (e: Exception) {
            println("PublicRepository.getSiteSettings error: ${e.message}")
            emit(null)
        }
    }

    /**
     * Send contact message to POST /config/contact
     */
    fun submitContactInquiry(request: ContactInquiryRequest): Flow<ContactInquiryResponse> = flow {
        try {
            val response = httpClient.post("$BASE_URL/config/contact") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<ContactInquiryResponse>()
            emit(response)
        } catch (e: Exception) {
            println("PublicRepository.submitContactInquiry error: ${e.message}")
            emit(ContactInquiryResponse(success = false, message = e.message ?: "Failed to submit inquiry"))
        }
    }
}

