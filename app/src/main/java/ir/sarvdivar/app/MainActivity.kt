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
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        // WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                super.onPageFinished(view, url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePath: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                // اگر انتخاب قبلی هنوز باز مانده، آن را لغو کن
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = null

                if (filePath == null) {
                    return false
                }

                this@MainActivity.filePathCallback = filePath

                return try {

                    val intent = fileChooserParams?.createIntent()

                    if (intent == null) {
                        this@MainActivity.filePathCallback?.onReceiveValue(null)
                        this@MainActivity.filePathCallback = null
                        false
                    } else {

                        startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST_CODE
                        )

                        true
                    }

                } catch (e: Exception) {

                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                    this@MainActivity.filePathCallback = null

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

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode != FILE_CHOOSER_REQUEST_CODE) {
            return
        }

        val callback = filePathCallback

        // خیلی مهم: callback را همین‌جا آزاد می‌کنیم
        filePathCallback = null

        if (callback == null) {
            return
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            callback.onReceiveValue(null)
            return
        }

        try {

            val results = mutableListOf<Uri>()

            // حالت اول: چند فایل یا عکس انتخاب شده
            val clipData = data.clipData

            if (clipData != null && clipData.itemCount > 0) {

                for (i in 0 until clipData.itemCount) {

                    val uri = clipData.getItemAt(i).uri

                    if (uri != null) {
                        results.add(uri)
                    }
                }
            }

            // حالت دوم: یک عکس انتخاب شده
            if (results.isEmpty()) {

                val uri = data.data

                if (uri != null) {
                    results.add(uri)
                }
            }

            // حالت سوم: اگر دستگاه نتیجه را به روش استاندارد WebView داده باشد
            if (results.isEmpty()) {

                val parsedResults =
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )

                if (parsedResults != null) {

                    for (uri in parsedResults) {

                        if (uri != null) {
                            results.add(uri)
                        }
                    }
                }
            }

            // نتیجه نهایی را مستقیماً به WebView بده
            if (results.isNotEmpty()) {

                callback.onReceiveValue(
                    results.toTypedArray()
                )

            } else {

                callback.onReceiveValue(null)
            }

        } catch (e: Exception) {

            callback.onReceiveValue(null)
        }
    }

    override fun onDestroy() {

        filePathCallback?.onReceiveValue(null)
        filePathCallback = null

        webView.destroy()

        super.onDestroy()
    }
}
