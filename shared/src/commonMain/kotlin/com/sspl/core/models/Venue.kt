package com.sspl.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Venue(
    val id: Int?,
    val title: String?,
    val address: String?,
    val lat: Double?,
    val lng: Double?
)