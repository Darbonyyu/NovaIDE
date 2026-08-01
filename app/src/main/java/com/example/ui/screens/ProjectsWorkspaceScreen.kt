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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.ui.components.workspace.*

@Composable
fun ProjectsWorkspaceScreen(viewModel: IdeViewModel) {
    val currentFiles by viewModel.currentFiles.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val isTerminalOpen by viewModel.isTerminalOpen.collectAsState()
    val isLivePreviewOpen by viewModel.isLivePreviewOpen.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()

    var showFileTree by remember { mutableStateOf(true) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showCreateFileModal by remember { mutableStateOf(false) }
    var showCreateFolderModal by remember { mutableStateOf(false) }
    var showProjectSettingsModal by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var renameProjectText by remember(currentProject) { mutableStateOf(currentProject?.name ?: "") }
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
                        onClick = { showCreateFolderModal = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            tint = AccentOrange
                        )
                    }
                    IconButton(
                        onClick = { showProjectSettingsModal = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Project Settings",
                            tint = MaterialTheme.colorScheme.onSurface
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

                        com.example.ui.components.workspace.FileExplorer(
                            currentFiles = currentFiles,
                            activeTabId = activeTabId,
                            onOpenFile = { viewModel.openFileTab(it) },
                            onDeleteFile = { viewModel.deleteFile(it) }
                        )
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
                com.example.ui.components.workspace.WorkspaceEditor(
                    fontSize = settings.fontSize,
                    activeFile = activeFile,
                    isLivePreviewOpen = isLivePreviewOpen,
                    editorTextState = editorTextState,
                    onCodeChange = {
                        editorTextState = it
                        viewModel.updateActiveFileContent(it)
                    }
                )
            }
        }

        // Integrated Bottom Terminal Drawer
        com.example.ui.components.workspace.TerminalView(
            fontSize = settings.fontSize - 2,
            isTerminalOpen = isTerminalOpen,
            terminalLogs = terminalLogs,
            terminalInput = terminalInput,
            onTerminalInputChange = { terminalInput = it },
            onCloseTerminal = { viewModel.toggleTerminal() },
            onRunCommand = { cmd -> 
                viewModel.runTerminalCommand(cmd)
                terminalInput = ""
            }
        )
    }


    // Modal Create New Folder
    if (showCreateFolderModal) {
        com.example.ui.components.workspace.CreateFolderDialog(
            folderName = newFolderName,
            onFolderNameChange = { newFolderName = it },
            onDismiss = { showCreateFolderModal = false },
            onCreate = {
                viewModel.createFolder(newFolderName)
                showCreateFolderModal = false
                newFolderName = ""
            }
        )
    }

    val proj = currentProject
    if (showProjectSettingsModal && proj != null) {
        com.example.ui.components.workspace.ProjectSettingsDialog(
            project = proj,
            projectName = renameProjectText,
            onProjectNameChange = { renameProjectText = it },
            onDismiss = { showProjectSettingsModal = false },
            onSave = {
                viewModel.renameProject(proj.id, renameProjectText)
                showProjectSettingsModal = false
            },
            onDelete = {
                viewModel.deleteProject(proj.id)
                showProjectSettingsModal = false
            }
        )
    }

    if (showCreateFileModal) {
        com.example.ui.components.workspace.CreateFileDialog(
            fileName = newFileNameInput,
            onFileNameChange = { newFileNameInput = it },
            onDismiss = { showCreateFileModal = false },
            onCreate = {
                val activeParent = activeFile?.parentPath ?: ""
                viewModel.createNewFile(activeParent, newFileNameInput)
                showCreateFileModal = false
                newFileNameInput = ""
            }
        )
    }
}
