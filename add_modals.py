import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

decls = """    var showCreateFileModal by remember { mutableStateOf(false) }
    var showCreateFolderModal by remember { mutableStateOf(false) }
    var showProjectSettingsModal by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var renameProjectText by remember(currentProject) { mutableStateOf(currentProject?.name ?: "") }"""

content = re.sub(r'    var showCreateFileModal by remember \{ mutableStateOf\(false\) \}', decls, content)

modals = """
    // Modal Create New Folder
    if (showCreateFolderModal) {
        AlertDialog(
            onDismissRequest = { showCreateFolderModal = false },
            title = { Text("Создать папку", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Введите название папки:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newFolderName.isNotBlank()) {
                        viewModel.createFolder(newFolderName)
                        newFolderName = ""
                        showCreateFolderModal = false
                    }
                }) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderModal = false }) { Text("Отмена") }
            }
        )
    }

    // Modal Project Settings
    if (showProjectSettingsModal && currentProject != null) {
        AlertDialog(
            onDismissRequest = { showProjectSettingsModal = false },
            title = { Text("Настройки проекта", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Переименовать проект:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = renameProjectText,
                        onValueChange = { renameProjectText = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.deleteProject(currentProject.id)
                            showProjectSettingsModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить проект")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (renameProjectText.isNotBlank() && renameProjectText != currentProject.name) {
                        viewModel.renameProject(currentProject.id, renameProjectText)
                    }
                    showProjectSettingsModal = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProjectSettingsModal = false }) { Text("Отмена") }
            }
        )
    }
"""

content = content.replace('    // Modal Create New File', modals + '\n    // Modal Create New File')

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
