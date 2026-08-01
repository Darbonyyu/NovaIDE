import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

# Replace CreateFolderModal
content = re.sub(
    r'    // Modal Create New Folder\n    if \(showCreateFolderModal\) \{[\s\S]*?\n            \)\n        \}\n    \}',
    '''    // Modal Create New Folder
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
    }''',
    content
)

# Replace ProjectSettingsModal
content = re.sub(
    r'    val proj = currentProject\n    if \(showProjectSettingsModal && proj != null\) \{[\s\S]*?\n            \)\n        \}\n    \}',
    '''    val proj = currentProject
    if (showProjectSettingsModal && proj != null) {
        com.example.ui.components.workspace.ProjectSettingsDialog(
            project = proj,
            projectName = renameProjectText,
            onProjectNameChange = { renameProjectText = it },
            onDismiss = { showProjectSettingsModal = false },
            onSave = {
                viewModel.updateProject(proj.copy(name = renameProjectText))
                showProjectSettingsModal = false
            },
            onDelete = {
                // Delete logic if needed
            }
        )
    }''',
    content
)

# Replace CreateFileModal
content = re.sub(
    r'    // Modal Create New File\n    if \(showCreateFileModal\) \{[\s\S]*?\n            \)\n        \}\n    \}',
    '''    // Modal Create New File
    if (showCreateFileModal) {
        com.example.ui.components.workspace.CreateFileDialog(
            fileName = newFileNameInput,
            onFileNameChange = { newFileNameInput = it },
            onDismiss = { showCreateFileModal = false },
            onCreate = {
                // Determine path based on selected folder in tree (for now just root or src)
                // Simplify for refactoring, just use "src" as default or active file's parent
                val activeParent = activeFile?.parentPath ?: "src"
                viewModel.createNewFile(activeParent, newFileNameInput)
                showCreateFileModal = false
                newFileNameInput = ""
            }
        )
    }''',
    content
)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
