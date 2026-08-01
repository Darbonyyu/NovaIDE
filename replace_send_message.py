import re

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

replacement = """
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
                attachedFilesJson = JSONArray(attachments).toString()
            )
            repository.insertChatMessage(userMsg)
            startStreamingResponse(userText)
        }
    }

    fun followUpCodeBlock(codeBlock: CodeBlock, followUpPrompt: String) {
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
                AiEngine.generateAiResponseStream(
                    prompt = prompt,
                    provider = provider,
                    projectFiles = files,
                    activeFile = active,
                    specificCodeToModify = specificCode
                ).collect { chunk ->
                    fullResponse += chunk
                    val parsed = AiEngine.parseResponseToAiResponse(fullResponse)
                    
                    val codeBlocksJson = JSONArray().apply {
                        parsed.codeBlocks.forEach { b ->
                            put(JSONObject().apply {
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
                
                val parsed = AiEngine.parseResponseToAiResponse(fullResponse)
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
"""

pattern = r"    fun sendMessage\(userText: String\) \{.*?(?=    fun insertCodeToActiveTab)"
new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(new_content)
