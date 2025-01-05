package com.sspl.utils

import kotlinx.datetime.*
import kotlinx.datetime.format.*

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */

@OptIn(FormatStringsInDatetimeFormats::class)
fun formatStringToDate(dateString: String, pattern: String): LocalDateTime {
    return try {
        LocalDateTime.parse(dateString, LocalDateTime.Format { byUnicodePattern(pattern) })
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}