package ir.sarvdivar.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
        private const val SUBMIT_LISTING_URL =
            "https://sarvdivar.ir/submit-listing/"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.setSupportZoom(false)
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (url?.startsWith(SUBMIT_LISTING_URL) == true) {

                    val css = """
                        (function() {
                            var style = document.getElementById('sarvdivar-app-offset');

                            if (!style) {
                                style = document.createElement('style');
                                style.id = 'sarvdivar-app-offset';
                                style.innerHTML = 
                                    body {
                                        padding-top: 8px !important;
                                    }
                                ;
                                document.head.appendChild(style);
                            }
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(css, null)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePath: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                filePathCallback?.onReceiveValue(null)
                filePathCallback = filePath

                return try {
                    val intent = fileChooserParams?.createIntent()

                    if (intent != null) {
                        startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST_CODE
                        )
                        true
                    } else {
                        filePathCallback = null
                        false
                    }

                } catch (e: Exception) {
                    filePathCallback = null
                    false
                }
            }
        }

        webView.loadUrl("https://sarvdivar.ir/")

        setContentView(webView)

        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }
