package com.sspl.core.repositories

import com.sspl.core.models.AbstractSubmission
import com.sspl.core.models.AbstractSubmissionListResponse
import com.sspl.core.models.GenerateCertificateRequest
import com.sspl.core.models.GenerateCertificateResponse
import com.sspl.core.models.MembershipPlan
import com.sspl.core.models.MembershipPlanListResponse
import com.sspl.core.models.MembershipRegisterRequest
import com.sspl.core.models.Presentation
import com.sspl.core.models.UpgradeLifeMembershipRequest
import com.sspl.core.models.UserCertificate
import com.sspl.core.models.UserMembershipDetails
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

class PortalRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * D2/D3: Fetch membership plans
     */
    fun getMembershipPlans(): Flow<List<MembershipPlan>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/membership/plan").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<MembershipPlan> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["data"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            println("PortalRepository getMembershipPlans error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * D2: Fetch current user membership details
     */
    fun getMyMembership(): Flow<UserMembershipDetails?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/membership/my").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val details: UserMembershipDetails? = when {
                parsed is JsonObject && parsed["membership"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["membership"]!!.jsonObject)
                }
                parsed is JsonObject && parsed["data"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonObject)
                }
                parsed is JsonObject -> {
                    json.decodeFromJsonElement(parsed)
                }
                else -> null
            }
            emit(details)
        } catch (e: Exception) {
            println("PortalRepository getMyMembership error: ${e.message}")
            emit(null)
        }
    }

    /**
     * D2: Register for membership
     */
    suspend fun registerMembership(planId: Long): Result<String> {
        return try {
            val responseText = httpClient.post("$BASE_URL/membership/register") {
                contentType(ContentType.Application.Json)
                setBody(MembershipRegisterRequest(planId = planId))
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * D2: Upgrade to Life Membership
     */
    suspend fun upgradeToLifeMembership(planId: Long, phone: String?, profileFields: Map<String, String>?): Result<String> {
        return try {
            val responseText = httpClient.post("$BASE_URL/membership/upgrade-life") {
                contentType(ContentType.Application.Json)
                setBody(UpgradeLifeMembershipRequest(
                    planId = planId,
                    accountPhone = phone,
                    profile = profileFields
                ))
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * D7: Fetch user's submitted abstracts
     */
    fun getMySubmissions(status: String? = null): Flow<List<AbstractSubmission>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/abstractSubmission/") {
                if (!status.isNullOrBlank() && status != "ALL") {
                    parameter("status", status)
                }
            }.body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<AbstractSubmission> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["data"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            println("PortalRepository getMySubmissions error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * D6: Submit an abstract
     */
    suspend fun submitAbstract(
        title: String,
        authors: String,
        affiliations: String,
        conferenceId: Long?,
        meetingId: Long?
    ): Result<String> {
        return try {
            val map = buildMap {
                put("title", title)
                put("authors", authors)
                put("affiliations", affiliations)
                if (conferenceId != null) put("conferenceId", conferenceId.toString())
                if (meetingId != null) put("meetingId", meetingId.toString())
            }
            val responseText = httpClient.post("$BASE_URL/abstractSubmission/create") {
                contentType(ContentType.Application.Json)
                setBody(map)
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * D8: Fetch presentations
     */
    fun getMyPresentations(): Flow<List<Presentation>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/presentations/my").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<Presentation> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["data"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            println("PortalRepository getMyPresentations error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * D9: Fetch user certificates
     */
    fun getMyCertificates(conferenceId: Long? = null): Flow<List<UserCertificate>> = flow {
        try {
            val url = if (conferenceId != null) "$BASE_URL/certificates/my?conferenceId=$conferenceId" else "$BASE_URL/certificates/my"
            val responseText = httpClient.get(url).body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<UserCertificate> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["certificates"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["certificates"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["records"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                }
                parsed is JsonObject && parsed["data"] is JsonArray -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                }
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            println("PortalRepository getMyCertificates error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * D9: Generate session certificate
     */
    suspend fun generateCertificate(sessionId: Long?, personId: Long?, conferenceId: Long?): Result<GenerateCertificateResponse> {
        return try {
            val response = httpClient.post("$BASE_URL/conferences/certificate/generate") {
                contentType(ContentType.Application.Json)
                setBody(GenerateCertificateRequest(sessionId, personId, conferenceId))
            }.body<GenerateCertificateResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

