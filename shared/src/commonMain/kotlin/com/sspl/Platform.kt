package com.sspl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform