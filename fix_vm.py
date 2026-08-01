with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

replacement_createnewfile = """    fun createNewFile(path: String, filename: String, content: String = "", isFolder: Boolean = false) {
        val projId = activeProjectId.value
        val ext = if (isFolder) "" else filename.substringAfterLast('.', "")
        val fullPath = if (path.isBlank()) filename else "$path/$filename"
        viewModelScope.launch {
            val file = ProjectFile(
                projectId = projId,
                path = fullPath,
                filename = filename,
                extension = ext,
                content = content,
                isFolder = isFolder,
                parentPath = path
            )
            val newId = repository.saveFile(file)
            if (!isFolder) {
                openFileTab(file.copy(id = newId))
                showToast("Файл $filename создан")
            } else {
                showToast("Папка $filename создана")
            }
        }
    }"""

content = content.replace("""    fun createNewFile(path: String, filename: String, content: String = "", isFolder: Boolean = false) {
        val projId = activeProjectId.value
        val ext = filename.substringAfterLast('.', "")
        val fullPath = if (path.isBlank()) filename else "$path/$filename"
        viewModelScope.launch {
            val file = ProjectFile(
                projectId = projId,
                path = fullPath,
                filename = filename,
                extension = ext,
                content = content,
                isFolder = isFolder,
                parentPath = path
            )
            val newId = repository.saveFile(file)
            if (!isFolder) {
                openFileTab(file.copy(id = newId))
            }
            showToast("Файл $filename создан")
        }
    }""", replacement_createnewfile)


replacement_createfolder = """    fun createFolder(folderName: String) {
        createNewFile("", folderName, isFolder = true)
    }"""

import re
content = re.sub(r'    fun createFolder\(folderName: String\) \{[\s\S]*?\}\n    \}', replacement_createfolder, content)

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
