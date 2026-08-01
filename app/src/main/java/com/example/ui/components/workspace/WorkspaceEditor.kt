package com.example.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ProjectFile
import com.example.ui.components.CodeEditor

@Composable
fun WorkspaceEditor(
    activeFile: ProjectFile?,
    isLivePreviewOpen: Boolean,
    editorTextState: String,
    onCodeChange: (String) -> Unit
) {
    if (isLivePreviewOpen && activeFile != null) {
        LiveWebPreviewView(file = activeFile)
    } else if (activeFile != null) {
        // Modern Code Editor
        val lines = editorTextState.split("\n")
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0F))
        ) {
            // Line numbers
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color(0xFF10111A))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.indices.forEach { index ->
                    Text(
                        text = "${index + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            // Code Area
            CodeEditor(
                code = editorTextState,
                onCodeChange = onCodeChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            )
        }
    } else {
        // Empty Editor View
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Выберите файл слева для редактирования",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
