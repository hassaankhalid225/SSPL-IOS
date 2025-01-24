package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conference(
    val id: Int?,
    val title: String?,
    val summary: String?,
    @SerialName("start_date") val startDate: String?,
    @SerialName("end_date") val endDate: String?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("updated_at") val updatedAt: String?,
    val days: List<Day>
)