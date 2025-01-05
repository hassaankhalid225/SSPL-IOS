package com.sspl


import platform.Foundation.*
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.value


import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSStringEncodingVar
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.UIKit.UIDevice
import platform.darwin.NSObject
import platform.darwin.NSObjectMeta

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

//@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
//internal actual class SharedFileReader {
//    private val bundle: NSBundle = NSBundle.bundleForClass(BundleMarker)
//
//    actual fun loadJsonFile(fileName: String): String? {
//        val (filename, type) = when (val lastPeriodIndex = fileName.lastIndexOf('.')) {
//            0 -> {
//                null to fileName.drop(1)
//            }
//
//            in 1..Int.MAX_VALUE -> {
//                fileName.take(lastPeriodIndex) to fileName.drop(lastPeriodIndex + 1)
//            }
//
//            else -> {
//                fileName to null
//            }
//        }
//        val path = bundle.pathForResource(filename, type) ?: error(
//            "Couldn't get path of $fileName (parsed as: ${
//                listOfNotNull(
//                    filename,
//                    type
//                ).joinToString(".")
//            })"
//        )
//
//        return memScoped {
//            val errorPtr = alloc<ObjCObjectVar<NSError?>>().ptr
//
//            NSString.stringWithContentsOfFile(
//                path,
//                encoding = NSUTF8StringEncoding,
//                error = errorPtr
//            ) ?: run {
//                error("Couldn't load resource: $fileName. Error: ${errorPtr.pointed.value?.localizedDescription}")
//            }
//        }
//    }
//
//    private class BundleMarker : NSObject() {
//        companion object : NSObjectMeta()
//    }
//}

actual fun formatDateTime(dateTime: String, inFormat: String, outFormat: String): String {
    return try {
        val dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = inFormat  // Input format

        val date = dateFormatter.dateFromString(dateTime)
        dateFormatter.dateFormat = outFormat  // Output format
        dateFormatter.locale = NSLocale.localeWithLocaleIdentifier("en_US")

        date?.let { dateFormatter.stringFromDate(it) } ?: dateTime
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}