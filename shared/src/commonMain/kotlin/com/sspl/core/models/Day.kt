package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Day(
    val date: String?,
    val breaks: List<Break>?,
    @SerialName("sessions")
    val sessions: List<Session>?
)