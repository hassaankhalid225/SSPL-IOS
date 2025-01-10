package com.sspl.platform

import io.ktor.client.HttpClient

interface Platform {
    val name: String
}

expect fun getClient(): HttpClient

expect fun formatDateTime(dateTime: String, inFormat: String, outFormat: String): String