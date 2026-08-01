import re

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

methods = """
    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            // if deleted is active, close it
            if (_activeTabId.value == fileId) {
                closeFileTab(fileId)
            }
        }
    }
"""

content = content.replace('    fun clearChat() {', methods + '\n    fun clearChat() {')

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
