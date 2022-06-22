package com.example.productdealstrackerkotlin

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.web_view_browser_layout.*

class WebViewActivityAmazon : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view_browser_layout)

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.apply {
            loadUrl("https://www.amazon.in/")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
        }

        val getItemUrlButton = findViewById<FloatingActionButton>(R.id.getUrlFromBrowserFab)
        val url_textField = findViewById<TextInputEditText>(R.id.url_input)

        getItemUrlButton.setOnClickListener {

            println(webView.url)
            val intent = Intent()
            intent.putExtra("url",webView.url)
            setResult(Activity.RESULT_OK,intent)
//            webView.url?.let { it1 -> copyToClipboard(it1) }
            finish()
        }

    }

    override fun onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack()
        }else{
            super.onBackPressed()
        }
    }

    fun Context.copyToClipboard(text: String){
        val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("",text))
        Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
    }



}