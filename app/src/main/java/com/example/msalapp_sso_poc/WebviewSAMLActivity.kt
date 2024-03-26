package com.example.msalapp_sso_poc

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.msalapp_sso_poc.databinding.AcitivityWebviewSamlBinding


class WebviewSAMLActivity: AppCompatActivity() {
    private lateinit var binding: AcitivityWebviewSamlBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivityWebviewSamlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable Javascript (if required)
        binding.webviewSaml.settings.javaScriptEnabled = true

        // Enable HTML5 local and session storage
        binding.webviewSaml.settings.domStorageEnabled = true
        binding.webviewSaml.settings.databaseEnabled = true

        // Set webview client to handle page navigation
        binding.webviewSaml.webViewClient = WebViewClient()

        // Load the URL
        binding.webviewSaml.loadUrl("https://f812-49-207-220-205.ngrok-free.app/")

        // Optional: Handle page loading progress
        binding.webviewSaml.webViewClient = object : WebViewClient() {
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