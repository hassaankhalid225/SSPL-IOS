package com.sspl.platform.shared

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.Foundation.NSURL

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 30/01/2025.
 * se.muhammadimran@gmail.com
 */

@OptIn(ExperimentalForeignApi::class)
actual class UrlOpener {
    @OptIn(ExperimentalForeignApi::class)
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
//        val nsUrl = NSURL.URLWithString(url)
//        nsUrl?.let {
//            UIApplication.sharedApplication.openURL(it)
////            if (UIApplication.sharedApplication.respondsToSelector("openURL:options:completionHandler:")) {
////                UIApplication.sharedApplication.openURL(nsUrl)
////            }
//        }
    }
}