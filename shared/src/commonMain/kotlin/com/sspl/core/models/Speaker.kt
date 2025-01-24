package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Speaker(
    val id: Int?,
    val email: String?,
    val prefix: String?,
    @SerialName("first_name") val firstName: String?,
    @SerialName("last_name") val lastName: String?,
    val gender: String?
)