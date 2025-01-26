package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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