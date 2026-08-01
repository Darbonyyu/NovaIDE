import re
import os

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

# I will extract LiveWebPreviewView into LiveWebPreview.kt
live_web_preview_match = re.search(r'(@Composable\nfun LiveWebPreviewView[\s\S]*)', content)
if live_web_preview_match:
    with open('./app/src/main/java/com/example/ui/components/workspace/LiveWebPreview.kt', 'w') as f:
        f.write('''package com.example.ui.components.workspace

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.ProjectFile
import android.util.Base64

''' + live_web_preview_match.group(1))

    content = content.replace(live_web_preview_match.group(1), '')

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
