package com.sspl.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * In-app Swich gateway (android.webkit.WebView). Card / 3-D Secure pages stay inside the app;
 * only non-web schemes (bank / wallet apps via intent:, tel:, mailto:) are handed to the system.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal actual fun PaymentWebContent(
    url: String,
    state: PaymentWebState,
    onNavigate: (String) -> Boolean,
    onProgress: (Float) -> Unit,
    modifier: Modifier
) {
    val navigate by rememberUpdatedState(onNavigate)
    val progress by rememberUpdatedState(onProgress)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(false)
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true) // 3-D Secure

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, newProgress: Int) {
                        progress(newProgress / 100f)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val target = request.url.toString()
                        if (navigate(target)) return true
                        val scheme = request.url.scheme?.lowercase()
                        if (scheme == "http" || scheme == "https") return false
                        runCatching {
                            val intent = if (scheme == "intent") {
                                Intent.parseUri(target, Intent.URI_INTENT_SCHEME)
                            } else {
                                Intent(Intent.ACTION_VIEW, Uri.parse(target))
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            view.context.startActivity(intent)
                        }
                        return true
                    }

                    // Form POSTs to the return URL don't hit shouldOverrideUrlLoading — catch them here.
                    override fun onPageStarted(view: WebView, startedUrl: String?, favicon: Bitmap?) {
                        if (startedUrl != null && navigate(startedUrl)) view.stopLoading()
                    }
                }
                state.canGoBack = { canGoBack() }
                state.goBack = { goBack() }
                loadUrl(url)
            }
        },
        onRelease = { web ->
            web.stopLoading()
            web.destroy()
        }
    )
}
