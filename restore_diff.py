with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

diff_code = """
    private val _diffToCompare = MutableStateFlow<Pair<String, String>?>(null)
    val diffToCompare: StateFlow<Pair<String, String>?> = _diffToCompare.asStateFlow()

    fun openCodeDiff(generatedCode: String) {
        val original = activeFile.value?.content ?: ""
        _diffToCompare.value = Pair(original, generatedCode)
    }

    fun closeCodeDiff() {
        _diffToCompare.value = null
    }

    fun attachFileToChat"""

content = content.replace('    fun attachFileToChat', diff_code)
content = content.replace('fun updateSettings(newSettings: com.example.data.models.Settings)', 'fun updateSettings(newSettings: AppSettings)')

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
