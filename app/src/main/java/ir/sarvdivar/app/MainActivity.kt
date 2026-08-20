package ir.sarvdivar.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ایجاد WebView
        val webView = WebView(this)

        // فعال‌سازی تنظیمات مورد نیاز
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        // تنظیم WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

        // تنظیم WebChromeClient برای آپلود فایل
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

        // بارگذاری سایت
        webView.loadUrl("https://sarvdivar.ir/")

        // نمایش WebView
        setContentView(webView)

        // تنظیم padding برای رفع مشکل سایز صفحه
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            // حذف padding اضافی برای نمایش کامل صفحه
            view.setPadding(0, 0, 0, 0)
            insets
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {

            val results = if (resultCode == Activity.RESULT_OK && data != null) {
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            } else {
                null
            }

            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    override fun onBackPressed() {
        // پیدا کردن WebView
        val webView = findViewById<WebView>(android.R.id.content)

        // اگر WebView بتواند به صفحه قبل برگردد، این کار را می‌کند
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            // در غیر این صورت، برنامه بسته می‌شود
            super.onBackPressed()
        }
    }
}
