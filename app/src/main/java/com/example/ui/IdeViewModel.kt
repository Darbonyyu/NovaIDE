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

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IdeRepository = IdeRepository(AppDatabase.getInstance(application))

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
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            closeTab(fileId)
            showToast("Файл удален")
        }
    }

    fun attachFileToChat(fileName: String) {
        _attachedFiles.value = _attachedFiles.value + fileName
    }

    fun removeAttachedFile(fileName: String) {
        _attachedFiles.value = _attachedFiles.value.filter { it != fileName }
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
                attachedFilesJson = JSONArray(attachments).toString()
            )
            repository.insertChatMessage(userMsg)

            _isGenerating.value = true

            val provider = selectedProvider.value ?: ApiProvider(name = "Gemini", type = "GEMINI")
            val files = currentFiles.value
            val active = activeFile.value

            val aiResult = AiEngine.generateAiResponse(
                prompt = userText,
                provider = provider,
                projectFiles = files,
                activeFile = active
            )

            val codeBlocksJson = JSONArray().apply {
                aiResult.codeBlocks.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("code", b.code)
                        put("language", b.language)
                        put("explanation", b.explanation)
                    })
                }
            }.toString()

            val assistantMsg = ChatMessage(
                projectId = projId,
                sender = "assistant",
                content = aiResult.explanationText,
                timestamp = System.currentTimeMillis(),
                codeBlocksJson = codeBlocksJson
            )
            repository.insertChatMessage(assistantMsg)

            // Save history item
            if (aiResult.codeBlocks.isNotEmpty()) {
                repository.addHistory(
                    HistoryItem(
                        projectId = projId,
                        title = userText.take(40),
                        prompt = userText,
                        providerName = provider.name,
                        modelName = provider.selectedModel,
                        codeSnippet = aiResult.codeBlocks.first().code
                    )
                )
            }

            _isGenerating.value = false
        }
    }

    fun followUpCodeBlock(codeBlock: CodeBlock, followUpPrompt: String) {
        val projId = activeProjectId.value
        viewModelScope.launch {
            val userMsg = ChatMessage(
                projectId = projId,
                sender = "user",
                content = "Правка кода: \"$followUpPrompt\"",
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(userMsg)

            _isGenerating.value = true
            val provider = selectedProvider.value ?: ApiProvider(name = "Gemini", type = "GEMINI")

            val aiResult = AiEngine.generateAiResponse(
                prompt = followUpPrompt,
                provider = provider,
                projectFiles = currentFiles.value,
                activeFile = activeFile.value,
                specificCodeToModify = codeBlock.code
            )

            val codeBlocksJson = JSONArray().apply {
                aiResult.codeBlocks.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("code", b.code)
                        put("language", b.language)
                        put("explanation", b.explanation)
                    })
                }
            }.toString()

            val assistantMsg = ChatMessage(
                projectId = projId,
                sender = "assistant",
                content = aiResult.explanationText,
                timestamp = System.currentTimeMillis(),
                codeBlocksJson = codeBlocksJson
            )
            repository.insertChatMessage(assistantMsg)
            _isGenerating.value = false
        }
    }

    fun insertCodeToActiveTab(code: String) {
        val active = activeFile.value
        if (active != null) {
            val newContent = active.content + "\n\n" + code
            updateActiveFileContent(newContent)
            showToast("Код добавлен в ${active.filename}")
        } else {
            // Create a new file
            createNewFile("src", "GeneratedCode.kt", code)
        }
    }

    fun replaceActiveTabCode(code: String) {
        val active = activeFile.value
        if (active != null) {
            updateActiveFileContent(code)
            showToast("Содержимое ${active.filename} заменено")
        } else {
            createNewFile("src", "GeneratedCode.kt", code)
        }
    }

    fun saveCodeBlockAsFile(code: String, language: String) {
        val ext = when (language.lowercase()) {
            "kotlin", "kt" -> "kt"
            "typescript", "ts" -> "ts"
            "javascript", "js" -> "js"
            "python", "py" -> "py"
            "html" -> "html"
            "css" -> "css"
            "json" -> "json"
            else -> "txt"
        }
        val fileName = "AiModule_${System.currentTimeMillis() / 1000}.$ext"
        createNewFile("src", fileName, code)
    }

    fun openCodeDiff(generatedCode: String) {
        val original = activeFile.value?.content ?: "// No active file content"
        _diffToCompare.value = Pair(original, generatedCode)
    }

    fun closeCodeDiff() {
        _diffToCompare.value = null
    }

    fun selectProvider(provider: ApiProvider) {
        _selectedProvider.value = provider
        showToast("Выбрана модель: ${provider.name}")
    }

    fun testProviderConnection(provider: ApiProvider) {
        viewModelScope.launch {
            showToast("Тестирование соединения с ${provider.name}...")
            val (success, statusText, latency) = AiEngine.testConnection(provider)
            val updated = provider.copy(
                latencyMs = latency.toLong(),
                lastPingStatus = statusText
            )
            repository.saveProvider(updated)
            showToast("Результат: $statusText (${latency}ms)")
        }
    }

    fun saveProvider(provider: ApiProvider) {
        viewModelScope.launch {
            repository.saveProvider(provider)
            showToast("Провайдер ${provider.name} сохранен")
        }
    }

    fun deleteProvider(providerId: Long) {
        viewModelScope.launch {
            repository.deleteProvider(providerId)
            showToast("Провайдер удален")
        }
    }

    fun toggleTerminal() {
        _isTerminalOpen.value = !_isTerminalOpen.value
    }

    fun runTerminalCommand(command: String) {
        if (command.isBlank()) return
        val currentLogs = _terminalLogs.value.toMutableList()
        currentLogs.add("$$ $command")

        when (command.trim().lowercase()) {
            "help" -> {
                currentLogs.add("Available Commands:")
                currentLogs.add("  ls               - List project files")
                currentLogs.add("  cat <file>       - Display file content")
                currentLogs.add("  clear            - Clear console")
                currentLogs.add("  ai <prompt>      - Quick AI command execution")
            }
            "clear" -> {
                currentLogs.clear()
            }
            "ls" -> {
                val files = currentFiles.value
                if (files.isEmpty()) {
                    currentLogs.add("Directory empty.")
                } else {
                    files.forEach { f ->
                        currentLogs.add(if (f.isFolder) "📁 ${f.path}/" else "📄 ${f.path}")
                    }
                }
            }
            else -> {
                if (command.startsWith("cat ")) {
                    val filePath = command.substringAfter("cat ").trim()
                    val f = currentFiles.value.find { it.filename == filePath || it.path == filePath }
                    if (f != null) {
                        currentLogs.add(f.content.take(1000))
                    } else {
                        currentLogs.add("cat: $filePath: No such file")
                    }
                } else if (command.startsWith("ai ")) {
                    val prompt = command.substringAfter("ai ").trim()
                    currentLogs.add("[AI Agent]: Executing fast task for '$prompt'...")
                    sendMessage(prompt)
                } else {
                    currentLogs.add("bash: $command: command not found in sandbox")
                }
            }
        }
        _terminalLogs.value = currentLogs
    }

    fun toggleCommandPalette() {
        _isCommandPaletteOpen.value = !_isCommandPaletteOpen.value
    }

    fun toggleLivePreview() {
        _isLivePreviewOpen.value = !_isLivePreviewOpen.value
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
        showToast("Настройки обновлены")
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
}
