package com.sspl.platform

import com.sspl.storage.Storage
import io.ktor.client.HttpClient

interface Platform {
    val name: String
}


expect fun getStorage(): Storage
expect fun getClient(): HttpClient

expect fun formatDateTime(dateTime: String, inFormat: String, outFormat: String): String

