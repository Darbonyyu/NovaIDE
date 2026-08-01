import re

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun insertFile(file: ProjectFile): Long {
        return db.fileDao().insertFile(file)
    }
"""
content = content.replace('    suspend fun saveProjectFile(projectId: Long, filename: String, ext: String, content: String): Long {', repo_methods + '\n    suspend fun saveProjectFile(projectId: Long, filename: String, ext: String, content: String): Long {')

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)
