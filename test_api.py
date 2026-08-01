with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

replacement = """    fun testProviderConnection(provider: ApiProvider) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val (success, message, _) = com.example.ai.AiEngine.testConnection(provider.apiKey, provider)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (success) {
                    showToast("Успешно: $message")
                } else {
                    showToast("Ошибка: $message")
                }
            }
        }
    }"""
content = content.replace('    fun testProviderConnection(provider: ApiProvider) {\n        showToast("Testing provider ${provider.name}")\n    }', replacement)

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
