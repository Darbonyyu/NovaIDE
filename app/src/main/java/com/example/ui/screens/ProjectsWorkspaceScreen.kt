package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.ProjectFile
import com.example.ui.IdeViewModel
import com.example.ui.theme.*

@Composable
fun ProjectsWorkspaceScreen(viewModel: IdeViewModel) {
    val currentFiles by viewModel.currentFiles.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val isTerminalOpen by viewModel.isTerminalOpen.collectAsState()
    val isLivePreviewOpen by viewModel.isLivePreviewOpen.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()

    var showFileTree by remember { mutableStateOf(true) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showCreateFileModal by remember { mutableStateOf(false) }
    var terminalInput by remember { mutableStateOf("") }

    var editorTextState by remember(activeFile?.id) { mutableStateOf(activeFile?.content ?: "") }

    LaunchedEffect(activeFile?.content) {
        if (activeFile != null) {
            editorTextState = activeFile!!.content
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Workspace Control Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showFileTree = !showFileTree },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showFileTree) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = "Toggle Explorer",
                            tint = AccentIndigo
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (showFileTree) "Проводник" else "Редактор",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showCreateFileModal = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New File",
                            tint = AccentIndigo
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleLivePreview() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isLivePreviewOpen) Icons.Default.Code else Icons.Default.Visibility,
                            contentDescription = "Live Preview",
                            tint = if (isLivePreviewOpen) AccentEmerald else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleTerminal() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal",
                            tint = if (isTerminalOpen) AccentOrange else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Main Workspace Split Area (File Tree + Editor / Preview)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // File Tree Side Drawer
            AnimatedVisibility(visible = showFileTree) {
                Surface(
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Text(
                            text = "ФАЙЛЫ ПРОЕКТА",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(currentFiles) { file ->
                                val isSelected = file.id == activeTabId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) AccentIndigo.copy(alpha = 0.2f) else Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openFileTab(file) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                file.isFolder -> Icons.Default.Folder
                                                file.extension == "kt" -> Icons.Default.Code
                                                file.extension == "html" -> Icons.Default.Language
                                                file.extension == "json" -> Icons.Default.DataObject
                                                file.extension == "md" -> Icons.Default.Description
                                                else -> Icons.Default.InsertDriveFile
                                            },
                                            contentDescription = null,
                                            tint = if (file.isFolder) AccentOrange else if (isSelected) AccentIndigo else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = file.filename,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = if (isSelected) AccentIndigo else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Code Editor & Tabs Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Multi-Tab Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    openTabs.forEach { tabFile ->
                        val isActive = tabFile.id == activeTabId
                        Surface(
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                            color = if (isActive) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable { viewModel.openFileTab(tabFile) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tabFile.filename,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isActive) AccentIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Tab",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.closeTab(tabFile.id) }
                                )
                            }
                        }
                    }
                }

                // Editor Content / Live Preview Switch
                if (isLivePreviewOpen && activeFile != null) {
                    LiveWebPreviewView(file = activeFile!!)
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
                        OutlinedTextField(
                            value = editorTextState,
                            onValueChange = {
                                editorTextState = it
                                viewModel.updateActiveFileContent(it)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
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
        }

        // Integrated Bottom Terminal Drawer
        AnimatedVisibility(visible = isTerminalOpen) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                color = Color(0xFF090A0F),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Интегрированный Терминал", style = MaterialTheme.typography.labelSmall, color = AccentOrange)
                        }
                        IconButton(onClick = { viewModel.toggleTerminal() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close Terminal", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(terminalLogs) { log ->
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (log.startsWith("$$")) AccentIndigo else Color.LightGray
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$$ ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AccentIndigo)
                        OutlinedTextField(
                            value = terminalInput,
                            onValueChange = { terminalInput = it },
                            placeholder = { Text("команда (ls, cat, ai, help)...", fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        IconButton(
                            onClick = {
                                viewModel.runTerminalCommand(terminalInput)
                                terminalInput = ""
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.SubdirectoryArrowLeft, contentDescription = "Run", tint = AccentOrange)
                        }
                    }
                }
            }
        }
    }

    // Modal Create New File
    if (showCreateFileModal) {
        AlertDialog(
            onDismissRequest = { showCreateFileModal = false },
            title = { Text("Создать новый файл", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Введите название файла (например: AuthService.kt, index.html, config.json):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.isNotBlank()) {
                            viewModel.createNewFile("src", newFileNameInput)
                            newFileNameInput = ""
                            showCreateFileModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo, contentColor = Color.Black)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileModal = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun LiveWebPreviewView(file: ProjectFile) {
    val context = LocalContext.current

    if (file.extension == "html") {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadDataWithBaseURL(null, file.content, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, file.content, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Markdown or Code Preview Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AccentIndigo.copy(0.15f),
                border = BorderStroke(1.dp, AccentIndigo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = AccentIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Markdown Live Render Preview: ${file.filename}", style = MaterialTheme.typography.titleMedium, color = AccentIndigo)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = file.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}
