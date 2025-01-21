package com.sspl.platform


import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.sspl.IosStorage
import com.sspl.core.getBaseClient
import com.sspl.session.UserSession
import com.sspl.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.jetbrains.skia.Image
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.localeWithLocaleIdentifier
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual suspend fun getClient(userSession: UserSession): HttpClient {
    return getBaseClient(userSession = userSession, engineFactory = Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
//    return HttpClient(Darwin) {
//        install(ContentNegotiation) {
//            json(Json {
//                encodeDefaults = true
//                isLenient = true
//                coerceInputValues = true
//                ignoreUnknownKeys = true
//            })
//        }
//        install(Logging) {
//            logger = object : Logger {
//                override fun log(message: String) {
//                    Napier.d("HTTP Client", null, message)
//                }
//            }
//            level = LogLevel.ALL
//        }
//        engine {
//            configureRequest {
//                setAllowsCellularAccess(true)
//            }
//        }
//    }.also { Napier.base(DebugAntilog()) }
}

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

actual fun getStorage(): Storage = IosStorage()


actual fun ByteArray.toBitmap() = Image.makeFromEncoded(this).toComposeImageBitmap()


