package com.sspl.core.repositories

import com.sspl.core.models.FellowshipStats
import com.sspl.utils.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement

/** Non-2xx response from a fellowship endpoint. [message] is the backend's `message` when present. */
class FellowshipApiException(val status: Int, override val message: String) : Exception(message)

/**
 * Fellowship data layer. Mirrors `sspl-frotend/src/services/fellowshipApi.js` 1:1.
 *
 * The backend's fellowship payloads are config-driven and loosely typed (programme config, wizard
 * answers, dashboard envelopes), so the suspend APIs below return raw [JsonElement]s and the UI
 * layer (`ui/fellowship/FellowshipData.kt`) maps them. Every call throws on non-2xx
 * ([FellowshipApiException]) so screens can show a real error instead of an empty list.
 *
 * Bugs fixed vs the previous version:
 *  - `/fellowship/programs/open` records have `name` (no `title`) → the old typed decode threw and
 *    the hub always showed "no programmes".
 *  - `/fellowship/centers/directory` records have `institution_name` (no `name`) → same failure.
 *  - The HttpClient has no `expectSuccess`, so 4xx bodies were returned as "success" on submit.
 *  - Fellow/centre application payloads did not match the web contract.
 */
class FellowshipRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ---------------------------------------------------------------- helpers

    private suspend fun HttpResponse.jsonOrThrow(fallback: String): JsonElement {
        val text = runCatching { bodyAsText() }.getOrDefault("")
        val parsed = runCatching { json.parseToJsonElement(text) }.getOrNull()
        if (!status.isSuccess()) {
            val obj = parsed as? JsonObject
            val msg = (obj?.get("message") as? JsonPrimitive)?.contentOrNull
                ?: (obj?.get("error") as? JsonPrimitive)?.contentOrNull
            throw FellowshipApiException(status.value, msg?.takeIf { it.isNotBlank() } ?: fallback)
        }
        return parsed ?: JsonObject(emptyMap())
    }

    private fun JsonElement.records(): JsonArray = when (this) {
        is JsonArray -> this
        is JsonObject -> (this["records"] as? JsonArray) ?: (this["data"] as? JsonArray) ?: JsonArray(emptyList())
        else -> JsonArray(emptyList())
    }

    private fun JsonElement.asObject(): JsonObject = this as? JsonObject ?: JsonObject(emptyMap())

    private suspend fun getJson(path: String, fallback: String = "Request failed"): JsonElement =
        httpClient.get("$BASE_URL$path").jsonOrThrow(fallback)

    private suspend fun postJson(path: String, body: JsonElement, fallback: String = "Request failed"): JsonElement =
        httpClient.post("$BASE_URL$path") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.jsonOrThrow(fallback)

    private suspend fun putJson(path: String, body: JsonElement, fallback: String = "Request failed"): JsonElement =
        httpClient.put("$BASE_URL$path") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.jsonOrThrow(fallback)

    // ---------------------------------------------------------------- public (no auth on web)

    /** Legacy flow used by Home (never throws; emits zeros on failure). */
    fun getPublicStats(): Flow<FellowshipStats> = flow {
        emit(runCatching { publicStats() }.getOrElse { FellowshipStats() })
    }

    /** GET /fellowship/stats/public → plain object (or `{data:{…}}`). */
    suspend fun publicStats(): FellowshipStats {
        val el = getJson("/fellowship/stats/public", "Failed to load public fellowship stats")
        val obj = el.asObject()
        val target = (obj["data"] as? JsonObject) ?: obj
        return json.decodeFromJsonElement(target)
    }

    /** GET /fellowship/programs/open → `{records:[…]}`. */
    suspend fun openPrograms(): JsonArray =
        getJson("/fellowship/programs/open", "Fellowship programs could not be loaded.").records()

    /** GET /fellowship/centers/directory?program_id= → `{records:[…]}`. City/availability are filtered client-side like the web. */
    suspend fun centersDirectory(programId: Long? = null): JsonArray {
        val qs = if (programId != null) "?program_id=$programId" else ""
        return getJson("/fellowship/centers/directory$qs", "Failed to load centers directory").records()
    }

    /** GET /fellowship/centers/active?program_id= → `{records:[…]}` (used for preference dropdowns). */
    suspend fun activeCenters(programId: Long?): JsonArray {
        val qs = if (programId != null) "?program_id=$programId" else ""
        return getJson("/fellowship/centers/active$qs", "Failed to load active training centers").records()
    }

    /** POST /fellowship/upload-document (multipart `file`) → `{url | file_url}`. */
    suspend fun uploadDocument(bytes: ByteArray, fileName: String, mime: String): JsonObject =
        httpClient.post("$BASE_URL/fellowship/upload-document") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", bytes, Headers.build {
                            append(HttpHeaders.ContentType, mime)
                            append(HttpHeaders.ContentDisposition, "filename=\"${fileName.replace("\"", "")}\"")
                        })
                    }
                )
            )
        }.jsonOrThrow("Document upload failed").asObject()

    /** POST /fellowship/fellows (public). Payload built exactly like ApplicationWizard.jsx. */
    suspend fun submitFellowApplication(payload: JsonObject): JsonObject =
        postJson("/fellowship/fellows", payload, "Submission failed").asObject()

    /** POST /fellowship/training-centers (public). Payload built exactly like CenterApplicationWizard.jsx. */
    suspend fun submitCenterApplication(payload: JsonObject): JsonObject =
        postJson("/fellowship/training-centers", payload, "Submission failed").asObject()

    // ---------------------------------------------------------------- authenticated

    suspend fun fellowDashboard(): JsonObject =
        getJson("/fellowship/dashboard/fellow", "Failed to load dashboard").asObject()

    suspend fun trainerDashboard(): JsonObject =
        getJson("/fellowship/dashboard/trainer", "Failed to load dashboard").asObject()

    suspend fun clinicalStats(): JsonObject = getJson("/fellowship/dashboard/stats").asObject()

    suspend fun cases(query: String = "?limit=50"): JsonArray = getJson("/fellowship/cases$query").records()
    suspend fun createCase(payload: JsonObject): JsonElement = postJson("/fellowship/cases", payload)
    suspend fun reviewCase(id: Long, payload: JsonObject): JsonElement = putJson("/fellowship/cases/$id/review", payload)

    suspend fun attendance(query: String = "?limit=50"): JsonArray = getJson("/fellowship/attendance$query").records()
    suspend fun createAttendance(payload: JsonObject): JsonElement = postJson("/fellowship/attendance", payload)
    suspend fun verifyAttendance(id: Long, payload: JsonObject): JsonElement = putJson("/fellowship/attendance/$id/verify", payload)

    suspend fun assessments(query: String = "?limit=50"): JsonArray = getJson("/fellowship/assessments$query").records()
    suspend fun createAssessment(payload: JsonObject): JsonElement = postJson("/fellowship/assessments", payload)

    suspend fun trainerFellows(): JsonArray = getJson("/fellowship/trainer/fellows").records()

    suspend fun research(query: String = "?limit=50"): JsonArray = getJson("/fellowship/research$query").records()
    suspend fun reviewResearch(id: Long, payload: JsonObject): JsonElement = putJson("/fellowship/research/$id/review", payload)

    /** POST /fellowship/research — multipart form fields + optional `attachment` file, like the web. */
    suspend fun submitResearch(
        fields: Map<String, String>,
        attachment: ByteArray? = null,
        attachmentName: String? = null,
        attachmentMime: String = "application/octet-stream"
    ): JsonElement =
        httpClient.post("$BASE_URL/fellowship/research") {
            setBody(MultiPartFormDataContent(formData {
                fields.forEach { (k, v) -> append(k, v) }
                if (attachment != null) {
                    append("attachment", attachment, Headers.build {
                        append(HttpHeaders.ContentType, attachmentMime)
                        append(HttpHeaders.ContentDisposition, "filename=\"${(attachmentName ?: "attachment").replace("\"", "")}\"")
                    })
                }
            }))
        }.jsonOrThrow("Upload failed")

    suspend fun enrollmentProgress(enrollmentId: Long): JsonObject =
        getJson("/fellowship/enrollments/$enrollmentId/progress").asObject()

    suspend fun submitFinalEvaluation(payload: JsonObject): JsonElement = postJson("/fellowship/final-evaluations", payload)

    suspend fun myCertificates(): JsonArray = getJson("/fellowship/certificates?limit=5").records()

    suspend fun myFeedback(): JsonObject = getJson("/fellowship/feedback/my").asObject()
    suspend fun submitFeedbackPart1(payload: JsonObject): JsonObject = postJson("/fellowship/feedback/part1", payload).asObject()
    suspend fun submitFeedbackPart2(payload: JsonObject): JsonObject = postJson("/fellowship/feedback/part2", payload).asObject()
}
