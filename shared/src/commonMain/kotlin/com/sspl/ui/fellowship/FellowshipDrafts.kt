package com.sspl.ui.fellowship

import com.sspl.storage.Storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * On-device persistence for the fellowship wizards — the app equivalent of the web's localStorage keys
 * (ApplicationWizard.jsx / CenterApplicationWizard.jsx):
 *  - `sspl-fellow-draft:<programId>`,  `sspl-fellow-submitted:<programId>`
 *  - `sspl-center-draft:<programId>`,  `sspl-center-submitted:<programId>`
 * Values are JSON objects. Storage failures are swallowed (same as the web's try/catch).
 */
class FellowshipDraftStore(private val storage: Storage) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun read(key: String): JsonObject? = runCatching {
        storage.getString(key)?.takeIf { it.isNotBlank() }?.let { json.parseToJsonElement(it) as? JsonObject }
    }.getOrNull()

    suspend fun write(key: String, value: JsonObject) {
        runCatching { storage.putString(key, value.toString()) }
    }

    suspend fun remove(key: String) {
        runCatching { storage.remove(key) }
    }

    companion object {
        fun fellowDraftKey(programId: Long?) = "sspl-fellow-draft:${programId ?: "preview"}"
        fun fellowSubmittedKey(programId: Long?) = "sspl-fellow-submitted:${programId ?: "preview"}"
        fun centerDraftKey(programId: Long?) = "sspl-center-draft:${programId ?: "general"}"
        fun centerSubmittedKey(programId: Long?) = "sspl-center-submitted:${programId ?: "general"}"

        /** Debounce for draft writes (typing would otherwise hit storage on every keystroke). */
        const val SAVE_DEBOUNCE_MS = 600L
    }
}

// ---- tiny JSON helpers for draft (de)serialisation

internal fun Map<String, String>.toJsonObject(): JsonObject = JsonObject(mapValues { JsonPrimitive(it.value) })

internal fun List<String>.toJsonArray(): JsonArray = JsonArray(map(::JsonPrimitive))

internal fun JsonObject?.stringMap(key: String): Map<String, String> =
    (this?.get(key) as? JsonObject)?.mapNotNull { (k, v) ->
        (v as? JsonPrimitive)?.contentOrNull?.let { k to it }
    }?.toMap().orEmpty()

internal fun JsonObject?.stringList(key: String): List<String> =
    (this?.get(key) as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.orEmpty()

/** Preferences: String values, or List<String> for checkbox fields. */
internal fun Map<String, Any>.preferencesToJson(): JsonObject = JsonObject(
    mapNotNull { (k, v) ->
        val el: JsonElement? = when (v) {
            is String -> JsonPrimitive(v)
            is List<*> -> JsonArray(v.map { JsonPrimitive(it.toString()) })
            else -> null
        }
        el?.let { k to it }
    }.toMap()
)

internal fun JsonObject?.preferencesFromJson(key: String): Map<String, Any> =
    (this?.get(key) as? JsonObject)?.mapNotNull { (k, v) ->
        when (v) {
            is JsonPrimitive -> v.contentOrNull?.let { k to (it as Any) }
            is JsonArray -> k to (v.mapNotNull { (it as? JsonPrimitive)?.contentOrNull } as Any)
            else -> null
        }
    }?.toMap().orEmpty()
