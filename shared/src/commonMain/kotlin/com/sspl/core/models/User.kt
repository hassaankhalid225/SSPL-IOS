package com.sspl.core.models


import com.sspl.utils.EMAIL_GUEST_USER
import kotlinx.serialization.SerialName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class UserResponse(
    @SerialName("access_token")
    val accessToken: String?,

    @SerialName("refresh_token")
    val refreshToken: String? = null,

    @SerialName("user")
    val user: User?
)

@Serializable
data class UserMembershipPlan(
    val id: Long? = null,
    val name: String? = null,
    val title: String? = null
)

@Serializable
data class User(
    val id: Long = 0L,
    val prefix: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    val gender: String? = null,
    val role: String? = null,
    val roles: List<String> = emptyList(),

    @SerialName("profile_picture")
    val profilePicture: String? = null,

    val cnic: String? = null,

    @SerialName("membership_status")
    val membershipStatus: String? = null,

    @SerialName("membership_plan")
    val membershipPlan: UserMembershipPlan? = null,

    @SerialName("plan_id")
    val planId: Long? = null,

    @SerialName("profile")
    val profile: Profile? = null,

    @SerialName("account")
    val account: Account? = null
) {
    fun fullName() = "$firstName $lastName".trim()
    val fullName: String get() = fullName()
    fun isGuest() = account?.isGuestUser() == true

    val pmdcNo: String? get() = profile?.pmdcNumber
    val pmdc: String? get() = profile?.pmdcNumber
    val designation: String? get() = profile?.title
    val instituteName: String? get() = profile?.institute
    val institution: String? get() = profile?.institute
    val email: String? get() = account?.email
    val phone: String? get() = account?.phone
    val country: String? get() = profile?.country
    val city: String? get() = profile?.city
    val hospital: String? get() = profile?.hospital
    val address: String? get() = profile?.address
    val whatsapp: String? get() = profile?.contact?.whatsapp
}

@Serializable
data class Profile(
    val title: String? = null,
    val overview: String? = null,
    @SerialName("pmdc_number")
    val pmdcNumber: String? = null,
    val institute: String? = null,
    val hospital: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,

    @SerialName("org_number")
    val orgNumber: String? = null,

    // Education (web ProfilePage). Passing years may arrive as numbers or strings.
    @SerialName("mbbs_institute")
    val mbbsInstitute: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    @SerialName("mbbs_passing_year")
    val mbbsPassingYear: String? = null,
    @SerialName("fcps_institute")
    val fcpsInstitute: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    @SerialName("fcps_passing_year")
    val fcpsPassingYear: String? = null,
    @SerialName("frcs_institute")
    val frcsInstitute: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    @SerialName("frcs_passing_year")
    val frcsPassingYear: String? = null,
    @SerialName("ms_institute")
    val msInstitute: String? = null,
    @Serializable(with = LenientStringSerializer::class)
    @SerialName("ms_passing_year")
    val msPassingYear: String? = null,

    @SerialName("contact")
    val contact: Contact? = null
)

@Serializable
data class Contact(
    val whatsapp: String? = null,
    val skype: String? = null,
    val twitter: String? = null
)

/**
 * Decodes a JSON string, number or boolean as a String (null stays null) so fields such as
 * `mbbs_passing_year` survive whichever type the backend returns. Encodes as a plain string.
 */
@OptIn(ExperimentalSerializationApi::class)
object LenientStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LenientString", PrimitiveKind.STRING).nullable

    override fun deserialize(decoder: Decoder): String? {
        if (decoder is JsonDecoder) {
            val el = decoder.decodeJsonElement()
            return when (el) {
                is JsonNull -> null
                is JsonPrimitive -> el.content
                else -> el.toString()
            }
        }
        return if (decoder.decodeNotNullMark()) decoder.decodeString() else decoder.decodeNull()
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value)
    }
}

@Serializable
data class Account(
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null
) {
    fun isGuestUser() = email?.endsWith(EMAIL_GUEST_USER) == true
}

