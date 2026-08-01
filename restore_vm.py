with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

methods = """
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
            startStreamingResponse(userText)
        }
    }

    fun followUpCodeBlock(codeBlock: com.example.data.models.CodeBlock, followUpPrompt: String) {
        val projId = activeProjectId.value
        viewModelScope.launch {
            val userMsg = ChatMessage(
                projectId = projId,
                sender = "user",
                content = "Правка кода: \\"$followUpPrompt\\"",
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(userMsg)
            startStreamingResponse(followUpPrompt, codeBlock.code)
        }
    }

    private fun startStreamingResponse(prompt: String, specificCode: String? = null) {
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
                    specificCodeToModify = specificCode
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
                            content = "$fullResponse\\n\\n❌ Ошибка: ${e.message}",
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
        updateActiveFileContent(currentContent + "\\n" + code)
    }

    fun replaceActiveTabCode(code: String) {
        updateActiveFileContent(code)
    }

    fun saveCodeBlockAsFile(code: String, language: String) {
        createNewFile("", "generated_code.${if(language.isBlank()) "txt" else language}", code, false)
    }

    private val _diffToCompare = MutableStateFlow<Pair<String, String>?>(null)
    val diffToCompare: StateFlow<Pair<String, String>?> = _diffToCompare.asStateFlow()

    fun openCodeDiff(generatedCode: String) {
        val original = activeFile.value?.content ?: ""
        _diffToCompare.value = Pair(original, generatedCode)
    }

    fun closeCodeDiff() {
        _diffToCompare.value = null
    }

    fun attachFileToChat(uri: android.net.Uri, context: android.content.Context) {
        _attachedFiles.value = _attachedFiles.value + uri.toString()
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
            repository.insertProvider(provider) // repository uses insertProvider
        }
    }

    fun deleteProvider(id: Long) {
        viewModelScope.launch {
            repository.deleteProviderById(id)
        }
    }

    fun testProviderConnection(provider: ApiProvider) {
        showToast("Testing provider ${provider.name}")
    }

    fun updateSettings(newSettings: Settings) {
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
            repository.saveFile(newFolder)
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
content = content.replace('    fun clearChat() {', methods + '\n    fun clearChat() {')

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)

