package com.sspl.core.usecases

import com.sspl.core.models.ApiError
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Port of the web's `utils/apiError.js` (parseApiError / getFieldErrors) so auth & profile forms
 * show the same messages and field-level errors as the website.
 *
 * [rawMessage] is the server's own message (used by Login/Signup classification like the web),
 * [message] is the friendly message after the web's status/text overrides.
 */
data class ParsedApiError(
    val status: Int,
    val code: String?,
    val rawMessage: String?,
    val message: String,
    val fieldErrors: Map<String, String>
) {
    /** ApiError carrying the friendly message; `error` holds "HTTP_<status>" so callers can read it. */
    fun toApiError(): ApiError = ApiError(message = message, error = "HTTP_$status")
}

/** Reads "HTTP_<status>" written by [ParsedApiError.toApiError]; null for network/other errors. */
fun ApiError.httpStatus(): Int? =
    error?.takeIf { it.startsWith("HTTP_") }?.removePrefix("HTTP_")?.toIntOrNull()

private val STATUS_MESSAGES = mapOf(
    400 to "Invalid request. Please check your information.",
    401 to "Invalid email or password. Please check your credentials.",
    403 to "Access denied. You do not have permission to perform this action.",
    404 to "Account does not exist with this email. Please create an account.",
    409 to "An account with this email already exists. Please log in.",
    410 to "The requested resource is no longer available.",
    422 to "Please correct the highlighted fields.",
    500 to "Something went wrong on our end. Please try again later."
)

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

suspend fun HttpResponse.parseApiError(): ParsedApiError {
    val text = runCatching { bodyAsText() }.getOrNull()
    return parseApiErrorBody(status.value, text)
}

fun parseApiErrorBody(status: Int, body: String?): ParsedApiError {
    val data = body?.takeIf { it.isNotBlank() }?.let { runCatching { lenientJson.parseToJsonElement(it) }.getOrNull() }
    val obj = data as? JsonObject

    var code: String? = null
    var serverMessage: String? = null
    var details: JsonElement? = null
    if (obj != null) {
        val err = obj["error"]
        if (err is JsonObject && (err["message"] != null || err["code"] != null)) {
            code = err.str("code")
            serverMessage = err.str("message")
            details = err["details"]
        } else {
            code = (err as? JsonPrimitive)?.contentOrNull
            serverMessage = obj.messageText() ?: code
            details = obj["details"] ?: obj["errors"]
        }
    }

    val fieldErrors = normalizeFieldDetails(details)
    var message: String? = serverMessage?.takeUnless { isTechnicalMessage(it) }
    val lower = serverMessage.orEmpty().lowercase()

    if (status == 404 || lower == "not found" || lower.contains("user not found") ||
        lower.contains("account not found") || lower.contains("does not exist") ||
        lower.contains("no user") || lower.contains("no account")
    ) {
        message = STATUS_MESSAGES[404]
    } else if (status == 409 || lower == "conflict" || lower.contains("already exist") ||
        lower.contains("already registered") || lower.contains("already in use") ||
        lower.contains("already taken") || lower.contains("duplicate")
    ) {
        message = STATUS_MESSAGES[409]
    } else if (status == 401 && (serverMessage.isNullOrBlank() || lower == "unauthorized" ||
                lower.contains("401 unauthorized") || lower.contains("invalid credentials") ||
                lower.contains("invalid password") || lower.contains("wrong password"))
    ) {
        message = "Invalid email or password. Please check your credentials and try again."
    }
    if (message.isNullOrBlank() && fieldErrors.isNotEmpty()) message = "Please correct the highlighted fields."
    if (message.isNullOrBlank()) message = STATUS_MESSAGES[status] ?: STATUS_MESSAGES.getValue(500)

    return ParsedApiError(
        status = status,
        code = code,
        rawMessage = serverMessage,
        message = message,
        fieldErrors = fieldErrors
    )
}

private fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

/** `message` may be a string or (NestJS style) an array of strings. */
private fun JsonObject.messageText(): String? = when (val m = this["message"]) {
    is JsonPrimitive -> m.contentOrNull
    is JsonArray -> m.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString(" ").ifBlank { null }
    else -> null
}

private fun isTechnicalMessage(message: String): Boolean =
    Regex("request failed with status code \\d+", RegexOption.IGNORE_CASE).containsMatchIn(message) ||
        Regex("^HTTP_\\d+$", RegexOption.IGNORE_CASE).matches(message) ||
        Regex("^\\d{3}$").matches(message.trim())

private fun normalizeFieldDetails(details: JsonElement?): Map<String, String> {
    val out = linkedMapOf<String, String>()
    when (details) {
        is JsonArray -> details.forEach { item ->
            val o = item as? JsonObject ?: return@forEach
            val msg = o.str("message") ?: return@forEach
            out[o.str("field") ?: "root"] = msg
        }
        is JsonObject -> {
            val fields = details["fields"]
            if (fields is JsonArray) return normalizeFieldDetails(fields)
            flattenZod(details, "", out)
        }
        else -> Unit
    }
    return out
}

private fun flattenZod(details: JsonObject, prefix: String, out: MutableMap<String, String>) {
    details.forEach { (key, value) ->
        if (key == "_errors" && value is JsonArray && value.isNotEmpty()) {
            out[prefix.ifEmpty { "root" }] =
                value.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString(", ")
        } else if (value is JsonObject) {
            flattenZod(value, if (prefix.isEmpty()) key else "$prefix.$key", out)
        } else if (value is JsonArray && value.firstOrNull() is JsonPrimitive) {
            // `{ errors: { email: ["..."] } }` style — keep the first message per field.
            (value.first() as JsonPrimitive).contentOrNull?.let { out[if (prefix.isEmpty()) key else "$prefix.$key"] = it }
        } else if (value is JsonPrimitive && value.isString) {
            out[if (prefix.isEmpty()) key else "$prefix.$key"] = value.content
        }
    }
}

/** Web `PROFILE_FIELD_MAP` / `mapProfileFieldErrors`: API field path → profile form field key. */
private val PROFILE_FIELD_MAP = mapOf(
    "profile.contact.whatsapp" to "whatsapp", "contact.whatsapp" to "whatsapp",
    "profile.whatsapp" to "whatsapp", "whatsapp" to "whatsapp",
    "profile.pmdc_number" to "pmdc_number", "pmdc_number" to "pmdc_number",
    "profile.hospital" to "hospital", "hospital" to "hospital",
    "profile.address" to "address", "address" to "address",
    "profile.city" to "city", "city" to "city",
    "profile.country" to "country", "country" to "country",
    "profile.institute" to "institute", "institute" to "institute",
    "profile.mbbs_institute" to "mbbs_institute", "mbbs_institute" to "mbbs_institute",
    "profile.mbbs_passing_year" to "mbbs_passing_year", "mbbs_passing_year" to "mbbs_passing_year",
    "profile.fcps_institute" to "fcps_institute", "fcps_institute" to "fcps_institute",
    "profile.fcps_passing_year" to "fcps_passing_year", "fcps_passing_year" to "fcps_passing_year",
    "profile.frcs_institute" to "frcs_institute", "frcs_institute" to "frcs_institute",
    "profile.frcs_passing_year" to "frcs_passing_year", "frcs_passing_year" to "frcs_passing_year",
    "profile.ms_institute" to "ms_institute", "ms_institute" to "ms_institute",
    "profile.ms_passing_year" to "ms_passing_year", "ms_passing_year" to "ms_passing_year",
    "profile.profile_picture" to "profile_image", "profile_picture" to "profile_image",
    "account.email" to "email", "email" to "email",
    "account.phone" to "account_phone", "account_phone" to "account_phone", "phone" to "account_phone",
    "first_name" to "first_name", "last_name" to "last_name", "gender" to "gender"
)

fun mapProfileFieldErrors(fieldErrors: Map<String, String>): Map<String, String> =
    fieldErrors.entries.associate { (field, msg) ->
        (PROFILE_FIELD_MAP[field] ?: field.removePrefix("profile.")) to msg
    }
