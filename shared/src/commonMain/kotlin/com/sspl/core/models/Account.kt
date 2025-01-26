package com.sspl.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val email: String?,
    val user: User?
)
