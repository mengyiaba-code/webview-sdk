package com.mengyiaba.webviewsdk

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URLEncoder

class EasyWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    // Listener for app developers to receive payment success events
    var onPaymentSuccessListener: ((orderId: String, amount: String) -> Unit)? = null

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun setupWebView() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = WebViewClient() 
        addJavascriptInterface(PaymentBridge(), "AndroidSDK")
    }

    /**
     * Loads the bundled static.html file, generates the QR code, and starts polling.
     */
    fun showQrPayment(workerUrl: String, orderId: String, amount: String) {
        val encodedWorker = URLEncoder.encode(workerUrl, "UTF-8")
        val encodedId = URLEncoder.encode(orderId, "UTF-8")
        val encodedAmount = URLEncoder.encode(amount, "UTF-8")
        
        val localUrl = "file:///android_asset/static.html?id=$encodedId&amount=$encodedAmount&workerUrl=$encodedWorker"
        loadUrl(localUrl)
    }

    private inner class PaymentBridge {
        @JavascriptInterface
        fun onPaymentComplete(orderId: String, amount: String) {
            post {
                onPaymentSuccessListener?.invoke(orderId, amount)
            }
        }
    }
}
