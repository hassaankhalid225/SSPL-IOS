package com.sspl.core.repositories

import com.sspl.core.models.CreateCaseRequest
import com.sspl.core.models.FellowApplicationRequest
import com.sspl.core.models.FellowAssessment
import com.sspl.core.models.FellowAttendance
import com.sspl.core.models.FellowCase
import com.sspl.core.models.FellowDashboardResponse
import com.sspl.core.models.FellowResearch
import com.sspl.core.models.FellowshipFeedbackData
import com.sspl.core.models.FellowshipProgram
import com.sspl.core.models.FellowshipStats
import com.sspl.core.models.TrainerDashboardResponse
import com.sspl.core.models.TrainingCenter
import com.sspl.core.models.TrainingCenterApplicationRequest
import com.sspl.core.models.UserCertificate
import com.sspl.utils.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
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

class FellowshipRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * E1: Open Fellowship programs
     */
    fun getOpenPrograms(): Flow<List<FellowshipProgram>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/programs/open").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<FellowshipProgram> = when {
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
            println("FellowshipRepository getOpenPrograms error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * E1: Public fellowship stats
     */
    fun getPublicStats(): Flow<FellowshipStats> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/stats/public").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val stats: FellowshipStats = when {
                parsed is JsonObject && parsed["data"] is JsonObject -> {
                    json.decodeFromJsonElement(parsed["data"]!!.jsonObject)
                }
                parsed is JsonObject -> {
                    json.decodeFromJsonElement(parsed)
                }
                else -> FellowshipStats()
            }
            emit(stats)
        } catch (e: Exception) {
            println("FellowshipRepository getPublicStats error: ${e.message}")
            emit(FellowshipStats())
        }
    }

    /**
     * E2: Training centers directory
     */
    fun getTrainingCenters(city: String? = null): Flow<List<TrainingCenter>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/centers/directory") {
                if (!city.isNullOrBlank() && city != "ALL") {
                    parameter("city", city)
                }
            }.body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<TrainingCenter> = when {
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
            println("FellowshipRepository getTrainingCenters error: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * E4: Submit Fellow Application
     */
    suspend fun submitFellowApplication(request: FellowApplicationRequest): Result<String> {
        return try {
            val responseText = httpClient.post("$BASE_URL/fellowship/fellows") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * E5: Submit Training Center Application
     */
    suspend fun submitCenterApplication(request: TrainingCenterApplicationRequest): Result<String> {
        return try {
            val responseText = httpClient.post("$BASE_URL/fellowship/training-centers") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * E6: Fellow Dashboard
     */
    fun getFellowDashboard(): Flow<FellowDashboardResponse?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/dashboard/fellow").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val dashboard: FellowDashboardResponse? = when {
                parsed is JsonObject -> json.decodeFromJsonElement(parsed)
                else -> null
            }
            emit(dashboard)
        } catch (e: Exception) {
            println("FellowshipRepository getFellowDashboard error: ${e.message}")
            emit(null)
        }
    }

    /**
     * E7: Trainer Dashboard
     */
    fun getTrainerDashboard(): Flow<TrainerDashboardResponse?> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/dashboard/trainer").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val dashboard: TrainerDashboardResponse? = when {
                parsed is JsonObject -> json.decodeFromJsonElement(parsed)
                else -> null
            }
            emit(dashboard)
        } catch (e: Exception) {
            println("FellowshipRepository getTrainerDashboard error: ${e.message}")
            emit(null)
        }
    }

    /**
     * E6: Cases
     */
    fun getCases(): Flow<List<FellowCase>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/cases").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<FellowCase> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun createCase(request: CreateCaseRequest): Result<String> {
        return try {
            val responseText = httpClient.post("$BASE_URL/fellowship/cases") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reviewCase(caseId: Long, status: String, remarks: String?): Result<String> {
        return try {
            val responseText = httpClient.put("$BASE_URL/fellowship/cases/$caseId/review") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("status" to status, "supervisor_remarks" to (remarks ?: "")))
            }.body<String>()
            Result.success(responseText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Attendance
     */
    fun getAttendance(): Flow<List<FellowAttendance>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/attendance").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<FellowAttendance> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Assessments
     */
    fun getAssessments(): Flow<List<FellowAssessment>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/assessments").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<FellowAssessment> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Research
     */
    fun getResearch(): Flow<List<FellowResearch>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/research").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<FellowResearch> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Fellowship Certificates
     */
    fun getCertificates(): Flow<List<UserCertificate>> = flow {
        try {
            val responseText = httpClient.get("$BASE_URL/fellowship/certificates?limit=10").body<String>()
            val parsed = json.parseToJsonElement(responseText)
            val list: List<UserCertificate> = when {
                parsed is JsonArray -> json.decodeFromJsonElement(parsed)
                parsed is JsonObject && parsed["records"] is JsonArray -> json.decodeFromJsonElement(parsed["records"]!!.jsonArray)
                parsed is JsonObject && parsed["data"] is JsonArray -> json.decodeFromJsonElement(parsed["data"]!!.jsonArray)
                else -> emptyList()
            }
            emit(list)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

