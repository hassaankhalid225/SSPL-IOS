package com.sspl

import java.text.SimpleDateFormat
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatDateTime(dateTime: String, inFormat: String, outFormat: String): String {
    return try {
        println(">>>dateTime=$dateTime")
        val inputFormat = SimpleDateFormat(inFormat, Locale.getDefault())
        val date = inputFormat.parse(dateTime)

        val outputFormat = SimpleDateFormat(outFormat, Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}