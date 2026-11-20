package com.sspl.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIApplication
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/** In-app Swich gateway (WKWebView). Non-web schemes are handed to the system. */
@OptIn(ExperimentalForeignApi::class)
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
    // WKWebView holds its delegate weakly — keep a strong reference for the composable's lifetime.
    val delegate = remember { PaymentNavDelegate({ navigate(it) }, { progress(it) }) }

    UIKitView(
        factory = {
            WKWebView(frame = platform.CoreGraphics.CGRectZero.readValue(), configuration = WKWebViewConfiguration()).apply {
                navigationDelegate = delegate
                state.canGoBack = { canGoBack }
                state.goBack = { goBack() }
                NSURL.URLWithString(url)?.let { loadRequest(NSURLRequest.requestWithURL(it)) }
            }
        },
        modifier = modifier,
        onRelease = { it.stopLoading() }
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PaymentNavDelegate(
    private val onNavigate: (String) -> Boolean,
    private val onProgress: (Float) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        val target = decidePolicyForNavigationAction.request.URL
        val text = target?.absoluteString
        val scheme = target?.scheme?.lowercase()
        when {
            text != null && onNavigate(text) ->
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            scheme != null && scheme != "http" && scheme != "https" && scheme != "about" && scheme != "data" -> {
                UIApplication.sharedApplication.openURL(target)
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            }
            else -> decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onProgress(0.2f)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didCommitNavigation: WKNavigation?) {
        onProgress(0.6f)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onProgress(1f)
    }
}
