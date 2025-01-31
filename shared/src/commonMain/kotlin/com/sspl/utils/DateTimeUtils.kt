package com.sspl.utils

import com.sspl.platform.formatDateTime

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 05/01/2025.
 * se.muhammadimran@gmail.com
 */

const val FORMAT_DEFAULT_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val FORMAT_DAY_MONTH = "dd MMM"

const val FORMAT_TIME_24HOUR = "HH:mm"
const val FORMAT_TIME_12HOUR = "hh:mm a"

/**
 * Join to dates or time with hyphen
 * e.g 8:00 AM - 9:00 AM
 */
fun getJoinedDateTime(
    dateTime1: String,
    dateTime2: String,
    inFormat: String = FORMAT_DEFAULT_DATE_TIME,
    outFormat: String = FORMAT_DAY_MONTH
): String =
    "${
        formatDateTime(
            dateTime = dateTime1,
            inFormat = inFormat,
            outFormat = outFormat
        )
    } \u2014 ${
        formatDateTime(
            dateTime = dateTime2,
            inFormat = inFormat,
            outFormat = outFormat
        )
    }"