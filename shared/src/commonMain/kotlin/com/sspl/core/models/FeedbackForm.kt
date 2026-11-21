package com.sspl.core.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * GET /conferences/{id}/feedback-form. Decoding is tolerant: unknown question types fall back to
 * [QuestionType.UNKNOWN] (rendered as a text answer, like the web), and every optional field defaults.
 */
@Serializable
data class FeedbackForm(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("conference_id") val conferenceId: Long? = null,
    @SerialName("session_id") val sessionId: Long? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("question_groups") val questionGroups: List<QuestionGroup>? = null
)

@Serializable
data class QuestionGroup(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val questions: List<Question>? = null
)

@Serializable
data class Question(
    val id: Int,
    val statement: String? = null,
    val description: String? = null,
    @Serializable(with = QuestionTypeSerializer::class) val type: QuestionType = QuestionType.UNKNOWN,
    val options: List<@Serializable(with = OptionSerializer::class) Option>? = null,
    val statements: List<Statement>? = null
)

/** Answer choice; the API may send `{label, value}` objects or plain strings (web: `opt.value ?? opt.label ?? opt`). */
data class Option(
    val label: String,
    val value: String
)

@Serializable
data class Statement(
    val id: Int,
    val statement: String? = null
)

enum class QuestionType {
    EVALUATION,
    COMMENT,
    YESNO,
    CHOICE,
    /** Any type the app doesn't know yet — answered as free text, like the web's fallback. */
    UNKNOWN
}

object QuestionTypeSerializer : KSerializer<QuestionType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("QuestionType", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): QuestionType {
        val raw = runCatching { decoder.decodeString() }.getOrNull()?.trim()?.uppercase()
        return QuestionType.entries.firstOrNull { it.name == raw } ?: QuestionType.UNKNOWN
    }
    override fun serialize(encoder: Encoder, value: QuestionType) = encoder.encodeString(value.name)
}

object OptionSerializer : KSerializer<Option> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FeedbackOption", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Option {
        val element = (decoder as? JsonDecoder)?.decodeJsonElement()
            ?: return decoder.decodeString().let { Option(it, it) }
        return when (element) {
            is JsonObject -> {
                fun pick(key: String) = (element[key] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull
                val value = pick("value") ?: pick("label") ?: element.toString()
                val label = pick("label") ?: pick("value") ?: element.toString()
                Option(label = label, value = value)
            }
            is JsonPrimitive -> (element.contentOrNull ?: "").let { Option(it, it) }
            else -> element.toString().let { Option(it, it) }
        }
    }
    override fun serialize(encoder: Encoder, value: Option) {
        val json = encoder as? kotlinx.serialization.json.JsonEncoder
        if (json != null) json.encodeJsonElement(buildJsonObject { put("label", value.label); put("value", value.value) })
        else encoder.encodeString(value.value)
    }
}
