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
data class Session(
    val start: String,
    val end: String,
    @SerialName("break_start")
    val breakStart: String?,
    @SerialName("break_end")
    val breakEnd: String?,
    @SerialName("break_message")
    val breakMessage: String?,
    val title: String,
    val message: String,
    val speakers: List<String>,
    val hall: String
)
