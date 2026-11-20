package com.sspl.ui.portal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}

private fun mimeFor(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
    "pdf" -> "application/pdf"
    "ppt" -> "application/vnd.ms-powerpoint"
    "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    "doc" -> "application/msword"
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "mp4" -> "video/mp4"
    else -> "application/octet-stream"
}

@Composable
actual fun rememberPortalFilePicker(
    mimeTypes: List<String>,
    onPicked: (PickedFile?) -> Unit
): () -> Unit {
    val callback = rememberUpdatedState(onPicked)
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url == null) {
                    callback.value(null); return
                }
                val access = url.startAccessingSecurityScopedResource()
                val data = NSData.dataWithContentsOfURL(url)
                if (access) url.stopAccessingSecurityScopedResource()
                val name = url.lastPathComponent ?: "attachment"
                callback.value(
                    data?.let {
                        val size = it.length.toLong()
                        PickedFile(name, mimeFor(name), size, if (size > PORTAL_MAX_UPLOAD_BYTES) null else it.toByteArray())
                    }
                )
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                callback.value(null)
            }
        }
    }
    return remember {
        {
            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("public.item"),
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            )
            picker.delegate = delegate
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, true, null)
        }
    }
}
