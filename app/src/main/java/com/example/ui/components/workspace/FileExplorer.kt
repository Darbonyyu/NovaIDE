package com.example.ui.components.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ProjectFile
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange

@Composable
fun FileExplorer(
    currentFiles: List<ProjectFile>,
    activeTabId: Long?,
    onOpenFile: (ProjectFile) -> Unit,
    onDeleteFile: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(currentFiles) { file ->
            val isSelected = file.id == activeTabId
            var showMenu by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) AccentIndigo.copy(alpha = 0.2f) else Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFile(file) }
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
                            else -> Icons.AutoMirrored.Filled.InsertDriveFile
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(16.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(14.dp), tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteFile(file.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
