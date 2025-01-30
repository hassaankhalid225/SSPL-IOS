package com.sspl.platform.shared

import platform.UIKit.UIApplication
import platform.Foundation.NSURL

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 30/01/2025.
 * se.muhammadimran@gmail.com
 */


actual class UrlOpener {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}