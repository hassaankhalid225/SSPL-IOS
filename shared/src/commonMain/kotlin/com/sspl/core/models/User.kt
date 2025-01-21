package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val email: String,
    val role: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("is_premium")
    val isPremium: Boolean,
    val active: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)


@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val token: String?,
    val user: User
)