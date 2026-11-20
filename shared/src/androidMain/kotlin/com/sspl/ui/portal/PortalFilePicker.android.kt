package com.sspl.ui.portal

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPortalFilePicker(
    mimeTypes: List<String>,
    onPicked: (PickedFile?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            callback.value(null)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val picked = withContext(Dispatchers.IO) {
                runCatching {
                    val resolver = context.contentResolver
                    var name = "attachment"
                    var size = -1L
                    resolver.query(uri, null, null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val s = c.getColumnIndex(OpenableColumns.SIZE)
                            if (n >= 0) c.getString(n)?.let { name = it }
                            if (s >= 0 && !c.isNull(s)) size = c.getLong(s)
                        }
                    }
                    val mime = resolver.getType(uri) ?: "application/octet-stream"
                    if (size > PORTAL_MAX_UPLOAD_BYTES) {
                        PickedFile(name, mime, size, null)
                    } else {
                        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes == null) null
                        else if (bytes.size > PORTAL_MAX_UPLOAD_BYTES) PickedFile(name, mime, bytes.size.toLong(), null)
                        else PickedFile(name, mime, bytes.size.toLong(), bytes)
                    }
                }.getOrNull()
            }
            callback.value(picked)
        }
    }
    val types = mimeTypes.map { if (it == "any") "*/*" else it }.toTypedArray()
    return { launcher.launch(types) }
}
