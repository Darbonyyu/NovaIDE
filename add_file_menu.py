import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

replacement = """                                val isSelected = file.id == activeTabId
                                var showMenu by remember { mutableStateOf(false) }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) AccentIndigo.copy(alpha = 0.2f) else Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .androidx.compose.foundation.ExperimentalFoundationApi::class.java.let { // just a trick to avoid import
                                            androidx.compose.foundation.combinedClickable(
                                                onClick = { viewModel.openFileTab(file) },
                                                onLongClick = { showMenu = true }
                                            )
                                        }
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
                                        
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Удалить") },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.deleteFile(file.id)
                                                }
                                            )
                                        }
                                    }
                                }"""

# In Compose, to use combinedClickable we must opt-in to ExperimentalFoundationApi, or just add a DropdownMenu trigger via an IconButton
