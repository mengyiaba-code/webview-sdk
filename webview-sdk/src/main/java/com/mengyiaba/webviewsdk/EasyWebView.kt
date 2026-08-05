package com.mengyiaba.webviewsdk

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class EasyWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var onPaymentSuccessListener: ((orderId: String, amount: String) -> Unit)? = null

    @Volatile
    private var isPolling = false
    private var pollingThread: Thread? = null

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = WebViewClient()
    }

    fun showQrPayment(workerUrl: String, orderId: String, amount: String) {
        val encodedWorker = URLEncoder.encode(workerUrl, "UTF-8")
        val encodedId = URLEncoder.encode(orderId, "UTF-8")
        val encodedAmount = URLEncoder.encode(amount, "UTF-8")
        val localUrl = "file:///android_asset/static.html?id=$encodedId&amount=$encodedAmount&workerUrl=$encodedWorker"
        loadUrl(localUrl)

        startPolling(workerUrl, orderId, amount)
    }

    private fun startPolling(workerUrl: String, orderId: String, amount: String) {
        stopPolling()
        isPolling = true

        pollingThread = Thread {
            var currentStatus = ""

            while (isPolling) {
                try {
                    val targetUrl = URL("$workerUrl/payload?id=$orderId&_t=${System.currentTimeMillis()}")
                    val connection = targetUrl.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.useCaches = false
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().readText()
                        val json = JSONObject(response)
                        val payload = json.optJSONObject("payload")
                        val newStatus = payload?.optString("status") ?: ""

                        if (newStatus != currentStatus) {
                            currentStatus = newStatus

                            if (currentStatus == "scanned") {
                                post { evaluateJavascript("javascript:showScanned()", null) }
                            } else if (currentStatus == "paid") {
                                post { evaluateJavascript("javascript:showPaid()", null) }
                                post { onPaymentSuccessListener?.invoke(orderId, amount) }
                                isPolling = false
                            }
                        }
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (isPolling) {
                    Thread.sleep(1000)
                }
            }
        }
        pollingThread?.start()
    }

    fun stopPolling() {
        isPolling = false
        pollingThread?.interrupt()
        pollingThread = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPolling()
    }
}
