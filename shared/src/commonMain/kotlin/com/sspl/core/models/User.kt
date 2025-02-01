package com.sspl.core.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class UserResponse(
    @SerialName("access_token")
    val accessToken: String?,

    @SerialName("user")
    val user: User?
)

data class User(
    @SerialName("first_name")
    val firstName: String?,

    @SerialName("last_name")
    val lastName: String?,

    val gender: String?,
    val role: String?,

    @SerialName("profile")
    val profile: Profile?,

    @SerialName("account")
    val account: Account?
) {
    fun fullName() = "$firstName $lastName"
}
@Serializable
data class Profile(
    val title: String?,
    val overview: String?,
    @SerialName("pmdc_number")
    val pmdcNumber: String?,
    val institute: String?,
    val country: String?,

    @SerialName("org_number")
    val orgNumber: String?,

    @SerialName("contact")
    val contact: Contact?
)
@Serializable
data class Contact(
    val whatsapp: String?,
    val skype: String?,
    val twitter: String?
)

@Serializable
data class Account(
    val email: String?,
    val phone: String?
)

