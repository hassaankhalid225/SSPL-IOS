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
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.localeWithLocaleIdentifier
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIDevice

actual suspend fun getClient(userSession: UserSession): HttpClient {
    return getBaseClient(userSession = userSession, engineFactory = Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}

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




class IosGpsChecker : GpsChecker {
    override fun hasGps(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }

    override fun requestEnableLocationServices() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        settingsUrl?.let {
            if (UIApplication.sharedApplication.canOpenURL(it)) {
                UIApplication.sharedApplication.openURL(it)
            }
        }
    }
}

@Composable
actual fun createGpsChecker(): GpsChecker {
    return IosGpsChecker()
}


actual fun ByteArray.toBitmap() = Image.makeFromEncoded(this).toComposeImageBitmap()


