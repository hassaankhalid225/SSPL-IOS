package com.sspl.core.models


import com.sspl.utils.EMAIL_GUEST_USER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    fun isGuest() = account?.isGuestUser() == true
}

@Serializable
data class Profile(
    val title: String? = null,
    val overview: String? = null,
    @SerialName("pmdc_number")
    val pmdcNumber: String? = null,
    val institute: String? = null,
    val country: String? = null,

    @SerialName("org_number")
    val orgNumber: String? = null,

    @SerialName("contact")
    val contact: Contact? = null
)

@Serializable
data class Contact(
    val whatsapp: String?,
    val skype: String?,
    val twitter: String?
)

@Serializable
data class Account(
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null
) {
    fun isGuestUser() = email?.endsWith(EMAIL_GUEST_USER) == true
}

