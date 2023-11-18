package com.money.protect

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class AbonnementActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val URL = "https://protect-money.net/abonnement"
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_abonnement)

        webView = findViewById(R.id.webView)
        webView.apply {
            loadUrl(URL)
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
        }
    }

}