package com.money.protect

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback

class TikeramaActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val URL = "https://tikerama.com"
    private lateinit var button: ImageView
    @SuppressLint("MissingInflatedId", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tikerama)
        webView = findViewById(R.id.webViewTikerama)
        button = findViewById(R.id.linkToMainActivity)

        webView.apply {
            loadUrl(URL)
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
        }
        button.setOnClickListener{
            finish()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                }
            }

        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
}