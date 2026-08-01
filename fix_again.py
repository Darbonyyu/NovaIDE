with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

diff_code = """
    fun openCodeDiff(generatedCode: String) {
        val original = activeFile.value?.content ?: ""
        _diffToCompare.value = Pair(original, generatedCode)
    }

    fun closeCodeDiff() {
        _diffToCompare.value = null
    }

    fun attachFileToChat"""

content = content.replace('    fun attachFileToChat', diff_code)

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
