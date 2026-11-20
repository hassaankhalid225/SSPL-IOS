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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class EventRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Fetch events from GET /external/events
     * @param type Optional filter (CONFERENCE, WORKSHOP, PROGRAM, MONTHLY_MEETING)
     * @param active Whether to return only active events
     */
    fun getEvents(type: String? = null, active: Boolean? = true): Flow<List<EventItem>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/external/events") {
                if (!type.isNullOrBlank() && type != "ALL") {
                    parameter("type", type)
                }
                if (active != null) {
                    parameter("active", active.toString())
                }
            }.body<String>()

            val parsed = json.parseToJsonElement(responseText)
            val eventsList: List<EventItem> = when {
                parsed is JsonArray -> {
                    json.decodeFromJsonElement(parsed)
                }
                parsed is JsonObject && parsed["data"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["data"] is JsonObject && parsed["data"]!!.jsonObject["events"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonObject["events"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["events"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["events"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(eventsList)
        } catch (e: Exception) {
            println("EventRepository.getEvents error: ${e.message}")
            emit(emptyList())
        }
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
            val checkData: EventCheckData? = when {
                parsed is JsonObject && parsed["data"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["data"]!!)
                }
                parsed is JsonObject -> {
                    json.decodeFromJsonElement(parsed)
                }
                else -> null
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

