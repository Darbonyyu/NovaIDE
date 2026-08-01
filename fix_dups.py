import re

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

pattern = r"    fun deleteFile\(fileId: Long\).*?closeFileTab\(fileId\)\n            \}\n        \}\n    \}"
content = re.sub(pattern, "", content, count=1, flags=re.DOTALL)
content = content.replace("closeFileTab", "closeTab") # the actual function is closeTab

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

pattern_repo = r"    suspend fun deleteFile\(id: Long\) \{\n        db\.fileDao\(\)\.deleteFileById\(id\)\n    \}"
content = re.sub(pattern_repo, "", content, count=1, flags=re.DOTALL)

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)
