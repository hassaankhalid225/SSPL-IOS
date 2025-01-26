package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String?,

    @SerialName("account")
    val account: Account?
)
@Serializable
data class Account(
    val email: String?,
    val user: User?
)

@Serializable
data class User(
    @SerialName("first_name")
    val firstName: String?,

    @SerialName("gender")
    val gender: String?,

    @SerialName("id")
    val id: Int?,

    @SerialName("last_name")
    val lastName: String?
)