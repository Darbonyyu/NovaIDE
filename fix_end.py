import re
with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

# I will just append the missing modals if they are not there
if 'ProjectSettingsDialog' not in content:
    content = content[:-2] + """
    val proj = currentProject
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
"""
    with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
        f.write(content)
