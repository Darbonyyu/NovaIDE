package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiEngine
import com.example.data.db.AppDatabase
import com.example.data.models.*
import com.example.data.repository.IdeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IdeRepository = IdeRepository(AppDatabase.getInstance(application), application)

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProviders: StateFlow<List<ApiProvider>> = repository.allProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistory: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = repository.settings

    private val _activeProjectId = MutableStateFlow<Long>(1)
    val activeProjectId: StateFlow<Long> = _activeProjectId.asStateFlow()

    val currentProject: StateFlow<Project?> = combine(allProjects, activeProjectId) { projects, id ->
        projects.find { it.id == id } ?: projects.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentFiles: StateFlow<List<ProjectFile>> = activeProjectId
        .flatMapLatest { id -> repository.getFilesForProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = activeProjectId
        .flatMapLatest { id -> repository.getMessagesForProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _openTabs = MutableStateFlow<List<ProjectFile>>(emptyList())
    val openTabs: StateFlow<List<ProjectFile>> = _openTabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<Long?>(null)
    val activeTabId: StateFlow<Long?> = _activeTabId.asStateFlow()

    val activeFile: StateFlow<ProjectFile?> = combine(currentFiles, activeTabId) { files, tabId ->
        files.find { it.id == tabId } ?: openTabs.value.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedProvider = MutableStateFlow<ApiProvider?>(null)
    val selectedProvider: StateFlow<ApiProvider?> = combine(allProviders, _selectedProvider) { providers, selected ->
        selected ?: providers.firstOrNull { it.isEnabled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isTerminalOpen = MutableStateFlow(false)
    val isTerminalOpen: StateFlow<Boolean> = _isTerminalOpen.asStateFlow()

    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

    private val _isLivePreviewOpen = MutableStateFlow(false)
    val isLivePreviewOpen: StateFlow<Boolean> = _isLivePreviewOpen.asStateFlow()

    private val _diffToCompare = MutableStateFlow<Pair<String, String>?>(null)
    val diffToCompare: StateFlow<Pair<String, String>?> = _diffToCompare.asStateFlow()

    private val _attachedFiles = MutableStateFlow<List<String>>(emptyList())
    val attachedFiles: StateFlow<List<String>> = _attachedFiles.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "AI IDE Terminal v2.4 initialized [Android container]",
            "Type 'help' or 'ai <prompt>' for fast CLI execution."
        )
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun selectProject(projectId: Long) {
        _activeProjectId.value = projectId
        _openTabs.value = emptyList()
        _activeTabId.value = null
    }

    fun createProject(name: String, desc: String, language: String) {
        viewModelScope.launch {
            val id = repository.createProject(name, desc, language)
            _activeProjectId.value = id
            showToast("Проект '$name' успешно создан")
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            showToast("Проект удален")
        }
    }

    fun openFileTab(file: ProjectFile) {
        if (file.isFolder) return
        if (!_openTabs.value.any { it.id == file.id }) {
            _openTabs.value = _openTabs.value + file
        }
        _activeTabId.value = file.id
    }

    fun closeTab(fileId: Long) {
        val updated = _openTabs.value.filter { it.id != fileId }
        _openTabs.value = updated
        if (_activeTabId.value == fileId) {
            _activeTabId.value = updated.lastOrNull()?.id
        }
    }

    fun updateActiveFileContent(newContent: String) {
        val tabId = _activeTabId.value ?: return
        viewModelScope.launch {
            repository.updateFileContent(tabId, newContent)
        }
    }

    fun createNewFile(path: String, filename: String, content: String = "", isFolder: Boolean = false) {
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
    }




    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun editMessage(messageId: Long, newContent: String) {
        viewModelScope.launch {
            repository.updateChatMessage(messageId, newContent, "[]")
        }
    }

    fun regenerateMessage(messageId: Long) {
        viewModelScope.launch {
            val msg = repository.getMessageById(messageId) ?: return@launch
            val lastUserMsg = chatMessages.value.lastOrNull { it.sender == "user" && it.id < messageId }
            if (lastUserMsg != null) {
                repository.deleteMessage(messageId)
                startStreamingResponse(lastUserMsg.content)
            }
        }
    }

    private var currentGenerationJob: kotlinx.coroutines.Job? = null

    fun stopGeneration() {
        currentGenerationJob?.cancel()
        _isGenerating.value = false
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val projId = activeProjectId.value
        val attachments = _attachedFiles.value
        _attachedFiles.value = emptyList()

        viewModelScope.launch {
            val userMsg = ChatMessage(
                projectId = projId,
                sender = "user",
                content = userText,
                timestamp = System.currentTimeMillis(),
                attachedFilesJson = org.json.JSONArray(attachments).toString()
            )
            repository.insertChatMessage(userMsg)
            startStreamingResponse(userText, attachments = attachments)
        }
    }

    fun followUpCodeBlock(codeBlock: com.example.data.models.CodeBlock, followUpPrompt: String) {
        val projId = activeProjectId.value
        viewModelScope.launch {
            val userMsg = ChatMessage(
                projectId = projId,
                sender = "user",
                content = "Правка кода: \"$followUpPrompt\"",
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(userMsg)
            startStreamingResponse(followUpPrompt, codeBlock.code)
        }
    }

    private fun startStreamingResponse(prompt: String, specificCode: String? = null, attachments: List<String> = emptyList()) {
        currentGenerationJob?.cancel()
        currentGenerationJob = viewModelScope.launch {
            _isGenerating.value = true
            val provider = selectedProvider.value ?: ApiProvider(name = "Gemini", type = "GEMINI")
            val files = currentFiles.value
            val active = activeFile.value
            val projId = activeProjectId.value

            var messageId: Long = 0
            var fullResponse = ""

            try {
                com.example.ai.AiEngine.generateAiResponseStream(
                    prompt = prompt,
                    provider = provider,
                    projectFiles = files,
                    activeFile = active,
                    specificCodeToModify = specificCode,
                    attachedFiles = attachments
                ).collect { chunk ->
                    fullResponse += chunk
                    val parsed = com.example.ai.AiEngine.parseResponseToAiResponse(fullResponse)
                    
                    val codeBlocksJson = org.json.JSONArray().apply {
                        parsed.codeBlocks.forEach { b ->
                            put(org.json.JSONObject().apply {
                                put("id", b.id)
                                put("code", b.code)
                                put("language", b.language)
                                put("explanation", b.explanation)
                            })
                        }
                    }.toString()

                    if (messageId == 0L) {
                        messageId = repository.insertChatMessage(
                            ChatMessage(
                                projectId = projId,
                                sender = "assistant",
                                content = parsed.explanationText,
                                codeBlocksJson = codeBlocksJson,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        repository.updateChatMessage(
                            messageId = messageId,
                            content = parsed.explanationText,
                            codeBlocksJson = codeBlocksJson
                        )
                    }
                }
                
                val parsed = com.example.ai.AiEngine.parseResponseToAiResponse(fullResponse)
                if (parsed.codeBlocks.isNotEmpty()) {
                    repository.addHistory(
                        HistoryItem(
                            projectId = projId,
                            title = prompt.take(40),
                            prompt = prompt,
                            providerName = provider.name,
                            modelName = provider.selectedModel,
                            codeSnippet = parsed.codeBlocks.first().code
                        )
                    )
                }

            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    if (messageId == 0L) {
                        repository.insertChatMessage(
                            ChatMessage(
                                projectId = projId,
                                sender = "assistant",
                                content = "❌ Ошибка: ${e.message}",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        repository.updateChatMessage(
                            messageId = messageId,
                            content = "$fullResponse\n\n❌ Ошибка: ${e.message}",
                            codeBlocksJson = "[]"
                        )
                    }
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun insertCodeToActiveTab(code: String) {
        val currentContent = activeFile.value?.content ?: return
        updateActiveFileContent(currentContent + "\n" + code)
    }

    fun replaceActiveTabCode(code: String) {
        updateActiveFileContent(code)
    }

    fun saveCodeBlockAsFile(code: String, language: String) {
        createNewFile("", "generated_code.${if(language.isBlank()) "txt" else language}", code, false)
    }






    fun openCodeDiff(generatedCode: String) {
        val original = activeFile.value?.content ?: ""
        _diffToCompare.value = Pair(original, generatedCode)
    }

    fun closeCodeDiff() {
        _diffToCompare.value = null
    }

    fun attachFileToChat(filename: String) {
        _attachedFiles.value = _attachedFiles.value + filename
    }
    
    fun removeAttachedFile(uriStr: String) {
        _attachedFiles.value = _attachedFiles.value.filter { it != uriStr }
    }

    fun toggleTerminal() {
        _isTerminalOpen.value = !_isTerminalOpen.value
    }

    fun toggleLivePreview() {
        _isLivePreviewOpen.value = !_isLivePreviewOpen.value
    }
    
    fun toggleCommandPalette() {
        _isCommandPaletteOpen.value = !_isCommandPaletteOpen.value
    }
    
    fun selectProvider(provider: ApiProvider) {
        _selectedProvider.value = provider
    }

    fun saveProvider(provider: ApiProvider) {
        viewModelScope.launch {
            repository.saveProvider(provider) // repository uses insertProvider
        }
    }

    fun deleteProvider(id: Long) {
        viewModelScope.launch {
            repository.deleteProvider(id)
        }
    }

    fun testProviderConnection(provider: ApiProvider) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val (success, message, _) = com.example.ai.AiEngine.testConnection(provider)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (success) {
                    showToast("Успешно: $message")
                } else {
                    showToast("Ошибка: $message")
                }
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }
    
    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            closeTab(fileId)
        }
    }

    fun runTerminalCommand(cmd: String) {
        _terminalLogs.value = _terminalLogs.value + "> $cmd"
        _terminalLogs.value = _terminalLogs.value + "Command executed."
    }

    fun createFolder(folderName: String) {
        createNewFile("", folderName, isFolder = true)
    }

    fun renameProject(projectId: Long, newName: String) {
        viewModelScope.launch {
            val proj = repository.getProjectById(projectId)
            if (proj != null) {
                repository.updateProject(proj.copy(name = newName))
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat(activeProjectId.value)
            showToast("Чат очищен")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearHistory()
            val allProjs = allProjects.value
            for (p in allProjs) {
                repository.deleteProject(p.id)
            }
            val allProvs = allProviders.value
            for (p in allProvs) {
                repository.deleteProvider(p.id)
            }
            showToast("Все данные успешно удалены")
            // Re-seed default after clearing
            repository.seedInitialDataIfNeeded()
        }
    }

    fun exportProjectZip(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val files = repository.getFilesForProject(activeProjectId.value).first()
                val proj = allProjects.value.find { it.id == activeProjectId.value } ?: return@launch
                val zipFile = java.io.File(context.cacheDir, "${proj.name.replace(" ", "_")}.zip")
                java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zout ->
                    for (file in files) {
                        if (!file.isFolder) {
                            val entry = java.util.zip.ZipEntry(file.path)
                            zout.putNextEntry(entry)
                            zout.write(file.content.toByteArray())
                            zout.closeEntry()
                        }
                    }
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    showToast("Архив сохранен: ${zipFile.absolutePath}")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    showToast("Ошибка экспорта: ${e.message}")
                }
            }
        }
    }

    fun importProjectZip(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val projId = repository.createProject("Импортированный Проект", "Импортирован из архива", "Unknown")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.util.zip.ZipInputStream(inputStream).use { zin ->
                        var entry = zin.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val content = zin.readBytes().toString(Charsets.UTF_8)
                                val path = entry.name
                                val filename = path.substringAfterLast("/")
                                val ext = filename.substringAfterLast(".", "")
                                val pFile = ProjectFile(
                                    projectId = projId,
                                    path = path,
                                    filename = filename,
                                    extension = ext,
                                    content = content,
                                    parentPath = path.substringBeforeLast("/", "")
                                )
                                repository.saveFile(pFile)
                            }
                            entry = zin.nextEntry
                        }
                    }
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    selectProject(projId)
                    showToast("Проект успешно импортирован")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    showToast("Ошибка импорта: ${e.message}")
                }
            }
        }
    }

    fun importDeviceFileAndAttach(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                var fileName = "file_${System.currentTimeMillis()}"
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            fileName = it.getString(nameIndex) ?: fileName
                        }
                    }
                }

                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isImage = mimeType.startsWith("image") || 
                              fileName.endsWith(".png", true) || 
                              fileName.endsWith(".jpg", true) || 
                              fileName.endsWith(".jpeg", true) || 
                              fileName.endsWith(".webp", true)

                val contentText = context.contentResolver.openInputStream(uri)?.use { stream ->
                    if (isImage) {
                        val bytes = stream.readBytes()
                        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        "[Изображение $fileName (data:image/${fileName.substringAfterLast(".", "png")};base64,$base64)]"
                    } else {
                        val bytes = stream.readBytes()
                        try {
                            bytes.toString(Charsets.UTF_8)
                        } catch (e: Exception) {
                            "[Бинарный файл $fileName, размер: ${bytes.size} байт]"
                        }
                    }
                } ?: ""

                val ext = fileName.substringAfterLast(".", "txt")
                val pFile = ProjectFile(
                    projectId = activeProjectId.value,
                    path = fileName,
                    filename = fileName,
                    extension = ext,
                    content = contentText,
                    parentPath = ""
                )
                repository.saveFile(pFile)

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    attachFileToChat(fileName)
                    showToast("Файл $fileName прикреплен")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    showToast("Ошибка прикрепления: ${e.message}")
                }
            }
        }
    }
}
