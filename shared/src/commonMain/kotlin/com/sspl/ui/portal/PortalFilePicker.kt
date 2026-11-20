package com.sspl.ui.portal

import androidx.compose.runtime.Composable

/** Max upload size accepted by the web forms (100 MB). */
const val PORTAL_MAX_UPLOAD_BYTES: Long = 100L * 1024 * 1024

/**
 * A document the user picked. [bytes] is null when the file exceeds [PORTAL_MAX_UPLOAD_BYTES]
 * (we don't read it into memory then; the UI shows the web's size message instead).
 */
class PickedFile(
    val name: String,
    val mimeType: String,
    val size: Long,
    val bytes: ByteArray?
)

/**
 * Remembers a system document picker. Invoke the returned lambda to open it.
 * [mimeTypes] e.g. listOf("any") or the presentation types. [onPicked] gets null on cancel/failure.
 */
@Composable
expect fun rememberPortalFilePicker(
    mimeTypes: List<String>,
    onPicked: (PickedFile?) -> Unit
): () -> Unit
