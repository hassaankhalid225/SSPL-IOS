package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("title")
    val title: String?,

    @SerialName("overview")
    val overview: String?,

    @SerialName("designation")
    val designation: String?,

    @SerialName("pmdc_number")
    val pmdcNumber: String?,

    @SerialName("org_number")
    val orgNumber: String?,

    @SerialName("contact")
    val contact: ContactInfo?
)

@Serializable
data class ContactInfo(
    @SerialName("email")
    val email: String?,

    @SerialName("phone")
    val phone: String?,

    @SerialName("whatsapp")
    val whatsapp: String?
)