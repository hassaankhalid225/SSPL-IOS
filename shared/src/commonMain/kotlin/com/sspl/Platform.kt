package com.sspl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun formatDateTime(dateTime: String, inFormat: String, outFormat: String): String