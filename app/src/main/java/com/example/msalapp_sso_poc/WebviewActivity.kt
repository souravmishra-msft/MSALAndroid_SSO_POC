package com.example.msalapp_sso_poc

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.msalapp_sso_poc.databinding.ActivityWebviewBinding

class WebviewActivity: AppCompatActivity() {
    private lateinit var binding: ActivityWebviewBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable Javascript (if required)
        binding.webview.settings.javaScriptEnabled = true

        // Enable HTML5 local and session storage
        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.databaseEnabled = true

        // Set webview client to handle page navigation
        binding.webview.webViewClient = WebViewClient()

        // Load the URL
        binding.webview.loadUrl("https://myapps.microsoft.com")

        // Optional: Handle page loading progress
        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.webviewLoading.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.webviewLoading.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }

        binding.backToHome.setOnClickListener {
            finish()
        }
    }
}