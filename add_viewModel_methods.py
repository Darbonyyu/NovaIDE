import re

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
            // If it's a user message, we might want to regenerate? The requirement says "редактирование сообщений;".
        }
    }

    fun regenerateMessage(messageId: Long) {
        viewModelScope.launch {
            val msg = repository.getMessageById(messageId) ?: return@launch
            // Find the user message before this one
            // We can simplify by just getting the last user message, but accurately we should find previous one.
            // For now, let's just trigger startStreamingResponse with the last user message content
            val lastUserMsg = _chatMessages.value.lastOrNull { it.sender == "user" && it.id < messageId }
            if (lastUserMsg != null) {
                repository.deleteMessage(messageId)
                startStreamingResponse(lastUserMsg.content)
            }
        }
    }
"""

# Insert before "fun stopGeneration"
content = content.replace("    fun stopGeneration() {", methods + "\n    fun stopGeneration() {")

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
