import re

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

# 1. Remove duplicate diffToCompare around line 341
pattern = r"    private val _diffToCompare = MutableStateFlow<Pair<String, String>\?>\(null\)\n    val diffToCompare: StateFlow<Pair<String, String>\?> = _diffToCompare\.asStateFlow\(\)\n\n    fun openCodeDiff\(generatedCode: String\) \{\n        val original = activeFile\.value\?\.content \?: \"\"\n        _diffToCompare\.value = Pair\(original, generatedCode\)\n    \}\n\n    fun closeCodeDiff\(\) \{\n        _diffToCompare\.value = null\n    \}"
content = re.sub(pattern, "", content)

# 2. Fix attachFileToChat
content = content.replace('fun attachFileToChat(uri: android.net.Uri, context: android.content.Context) {', 'fun attachFileToChat(filename: String) {')
content = content.replace('_attachedFiles.value = _attachedFiles.value + uri.toString()', '_attachedFiles.value = _attachedFiles.value + filename')

# 3. Fix Settings import
content = content.replace('fun updateSettings(newSettings: Settings)', 'fun updateSettings(newSettings: com.example.data.models.Settings)')

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)

