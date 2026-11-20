package com.sspl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.GlassIconButton
import com.sspl.designsystem.icons.SsplIcons
import com.sspl.designsystem.theme.AppTheme

/** How the Swich gateway ended, read from its return URL. */
enum class PaymentOutcome { SUCCESS, FAILED }

/**
 * Swich returns to `<site>/payment/status?status=…&message=…&responseCode=…`.
 * Same rule as the web's PaymentStatusView.jsx: success when status == "success" or
 * responseCode is "0000"/"00", anything else is a failure. Returns null for every other URL.
 */
fun paymentOutcomeOf(url: String): PaymentOutcome? {
    val path = url.substringBefore('?').substringBefore('#').trimEnd('/')
    if (!path.endsWith("/payment/status")) return null
    val params = url.substringAfter('?', "").substringBefore('#')
        .split('&')
        .filter { it.isNotBlank() }
        .associate { it.substringBefore('=').lowercase() to it.substringAfter('=', "") }
    val status = params["status"]?.lowercase()
    val code = params["responsecode"]
    return if (status == "success" || code == "0000" || code == "00") PaymentOutcome.SUCCESS else PaymentOutcome.FAILED
}

/** Lets the platform web view expose its history to the shared back handling. */
class PaymentWebState {
    internal var canGoBack: () -> Boolean = { false }
    internal var goBack: () -> Unit = {}
}

/**
 * Platform web view. [onNavigate] is called for every navigation (incl. redirects and form posts);
 * return true to stop it loading. [onProgress] reports page load 0..1.
 */
@Composable
internal expect fun PaymentWebContent(
    url: String,
    state: PaymentWebState,
    onNavigate: (String) -> Boolean,
    onProgress: (Float) -> Unit,
    modifier: Modifier
)

/**
 * In-app payment sheet (full-screen dialog). The gateway runs inside the app — no hand-off to
 * Chrome. Closes itself when Swich returns to /payment/status and reports the outcome.
 * Back goes back inside the gateway first; leaving asks for confirmation.
 */
@Composable
fun PaymentWebView(
    url: String,
    onPaymentSuccess: () -> Unit,
    onPaymentFailed: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val webState = remember(url) { PaymentWebState() }
    var progress by remember(url) { mutableFloatStateOf(0f) }
    var finished by remember(url) { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    val host = remember(url) { url.substringAfter("://").substringBefore('/').removePrefix("www.") }

    fun finish(outcome: PaymentOutcome) {
        if (finished) return
        finished = true
        if (outcome == PaymentOutcome.SUCCESS) onPaymentSuccess() else onPaymentFailed()
    }

    Dialog(
        onDismissRequest = {
            if (webState.canGoBack()) webState.goBack() else confirmLeave = true
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Column(modifier.fillMaxSize().background(colors.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.headerGradient)
                    .statusBarsPadding()
                    .height(60.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = SsplIcons.Close,
                    contentDescription = "Close payment",
                    onClick = { confirmLeave = true }
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Secure payment",
                        style = AppTheme.typography.titleMedium.copy(color = Color.White),
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            SsplIcons.Lock, contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            host,
                            style = AppTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.75f)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // Real page-load progress (determinate) — allowed by the design rules.
            Box(Modifier.fillMaxWidth().height(2.dp)) {
                if (progress < 1f) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0.05f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = colors.accent,
                        trackColor = Color.Transparent,
                        drawStopIndicator = {}
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                PaymentWebContent(
                    url = url,
                    state = webState,
                    onNavigate = { next ->
                        val outcome = paymentOutcomeOf(next)
                        if (outcome != null) finish(outcome)
                        outcome != null
                    },
                    onProgress = { progress = it },
                    modifier = Modifier.fillMaxSize()
                )
                if (progress < 0.3f && !finished) {
                    Column(Modifier.fillMaxSize().background(colors.background).padding(20.dp)) {
                        com.sspl.designsystem.components.SkeletonDetail()
                    }
                }
            }
        }
    }

    if (confirmLeave) {
        ConfirmDialog(
            title = "Leave payment?",
            message = "Your payment isn't finished. If you already paid, it will still be confirmed — you can check its status later.",
            confirmText = "Leave",
            dismissText = "Stay",
            onConfirm = {
                confirmLeave = false
                if (!finished) {
                    finished = true
                    onClose()
                }
            },
            onDismiss = { confirmLeave = false }
        )
    }
}
