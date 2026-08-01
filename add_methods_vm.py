import re

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

methods = """
    fun createFolder(folderName: String) {
        val projId = activeProjectId.value
        viewModelScope.launch {
            val newFolder = ProjectFile(
                projectId = projId,
                filename = folderName,
                extension = "",
                content = "",
                isFolder = true,
                path = folderName
            )
            repository.insertFile(newFolder)
        }
    }

    fun renameProject(projectId: Long, newName: String) {
        viewModelScope.launch {
            val proj = repository.getProjectById(projectId)
            if (proj != null) {
                repository.insertProject(proj.copy(name = newName))
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProjectById(projectId)
            // if deleted is current, clear active
            if (activeProjectId.value == projectId) {
                _currentProject.value = null
                _currentFiles.value = emptyList()
                _activeFile.value = null
                _openFiles.value = emptyList()
                activeProjectId.value = 1L // Or whatever is left
                // Actually wait, let's just select the first available
                val all = repository.getAllProjects().firstOrNull() ?: emptyList()
                val next = all.firstOrNull { it.id != projectId }
                if (next != null) {
                    selectProject(next.id)
                } else {
                    activeProjectId.value = -1L
                }
            }
        }
    }
"""

content = content.replace('    fun clearChat() {', methods + '\n    fun clearChat() {')
# also fix firstOrNull error by importing kotlinx.coroutines.flow.firstOrNull
content = content.replace('import kotlinx.coroutines.flow.asStateFlow', 'import kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.flow.firstOrNull')

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
