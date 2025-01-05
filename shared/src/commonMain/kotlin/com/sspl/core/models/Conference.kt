package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */
@Serializable
data class Conference(
    val title: String,
    val address: String,
    @SerialName("short_message")
    val shortMessage: String,
    @SerialName("start_at")
    val startAt: String,
    @SerialName("end_at")
    val endAt: String,
    val schedule: List<Schedule>
)
