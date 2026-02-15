package com.raushan.upscagent.ui.reader

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.raushan.upscagent.R
import java.io.BufferedReader
import java.io.InputStreamReader

class ReaderActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var webView: WebView
    private lateinit var tvTextContent: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var tvReaderTitle: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        pdfView = findViewById(R.id.pdf_view)
        webView = findViewById(R.id.web_view)
        tvTextContent = findViewById(R.id.tv_text_content)
        scrollView = findViewById(R.id.scroll_text_reader)
        tvReaderTitle = findViewById(R.id.tv_reader_title)
        progressBar = findViewById(R.id.progress_reader)

        val fileUri = intent.getStringExtra("file_uri") ?: ""
        val fileName = intent.getStringExtra("file_name") ?: "Document"
        val fileType = intent.getStringExtra("file_type") ?: "txt"

        tvReaderTitle.text = fileName
        findViewById<ImageButton>(R.id.btn_back_reader).setOnClickListener { finish() }

        when (fileType) {
            "pdf" -> loadPdf(Uri.parse(fileUri))
            "html" -> loadHtml(Uri.parse(fileUri))
            "txt" -> loadText(Uri.parse(fileUri))
            "doc" -> loadText(Uri.parse(fileUri)) // Basic fallback
            else -> loadText(Uri.parse(fileUri))
        }
    }

    private fun loadPdf(uri: Uri) {
        pdfView.visibility = View.VISIBLE
        webView.visibility = View.GONE
        scrollView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        try {
            val inputStream = contentResolver.openInputStream(uri)
            pdfView.fromStream(inputStream)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .spacing(4)
                .onLoad { pages ->
                    progressBar.visibility = View.GONE
                    tvReaderTitle.text = "${tvReaderTitle.text} ($pages pages)"
                }
                .onError { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error loading PDF: ${error.message}", Toast.LENGTH_LONG).show()
                }
                .load()
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Cannot open PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadHtml(uri: Uri) {
        pdfView.visibility = View.GONE
        webView.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
        }

        webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            textZoom = 120
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        try {
            val htmlContent = readFileContent(uri)
            if (htmlContent != null) {
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            } else {
                webView.loadUrl(uri.toString())
            }
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Error loading HTML: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadText(uri: Uri) {
        pdfView.visibility = View.GONE
        webView.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        try {
            val content = readFileContent(uri)
            tvTextContent.text = content ?: "Unable to read file content"
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            tvTextContent.text = "Error reading file: ${e.message}"
        }
    }

    private fun readFileContent(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = StringBuilder()
                var line = reader.readLine()
                while (line != null) {
                    content.appendLine(line)
                    line = reader.readLine()
                }
                content.toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
