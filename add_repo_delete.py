import re

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun deleteFile(id: Long) {
        db.fileDao().deleteFileById(id)
    }
"""
content = content.replace('    suspend fun saveFile(file: ProjectFile): Long {', repo_methods + '\n    suspend fun saveFile(file: ProjectFile): Long {')

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)
