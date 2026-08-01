package com.example.ui.components.workspace

import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.ProjectFile

@Composable
fun LiveWebPreviewView(file: ProjectFile) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        update = { webView ->
            val html = if (file.extension == "html") {
                file.content
            } else {
                "<html><body><pre>${file.content}</pre></body></html>"
            }
            val encodedHtml = Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING)
            webView.loadData(encodedHtml, "text/html", "base64")
        }
    )
}
