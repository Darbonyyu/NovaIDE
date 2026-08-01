with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

# Remove the duplicate deleteProject and bad renameProject/createFolder
import re
pattern = r"    fun createFolder\(folderName: String\).*?activeProjectId\.value = -1L\n                \}\n            \}\n        \}\n    \}"
content = re.sub(pattern, "", content, flags=re.DOTALL)

# Add correct createFolder and renameProject before "fun clearChat"
correct_methods = """
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
                repository.updateProject(proj.copy(name = newName))
            }
        }
    }
"""
content = content.replace('    fun clearChat() {', correct_methods + '\n    fun clearChat() {')

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('''    suspend fun getProjectById(id: Long): Project? {
        return db.projectDao().getProjectById(id)
    }

    suspend fun deleteProjectById(id: Long) {
        db.projectDao().deleteProjectById(id)
        // Also clean up files and chat? Handled by relations or we can just let clearChat do it.
        db.chatDao().clearChatForProject(id)
    }''', '')

content = content.replace('''    suspend fun insertProject(project: Project): Long {''', '''    suspend fun getProjectById(id: Long): Project? {
        return db.projectDao().getProjectById(id)
    }

    suspend fun updateProject(project: Project) {
        db.projectDao().updateProject(project)
    }

    suspend fun insertProject(project: Project): Long {''')

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)


with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('    val openTabs by viewModel.openTabs.collectAsState()', '    val openTabs by viewModel.openTabs.collectAsState()\n    val currentProject by viewModel.currentProject.collectAsState()')

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
