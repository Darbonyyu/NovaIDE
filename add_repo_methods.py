import re

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun getProjectById(id: Long): Project? {
        return db.projectDao().getProjectById(id)
    }

    suspend fun deleteProjectById(id: Long) {
        db.projectDao().deleteProjectById(id)
        // Also clean up files and chat? Handled by relations or we can just let clearChat do it.
        db.chatDao().clearChatForProject(id)
    }
"""
content = content.replace('    suspend fun insertProject(project: Project): Long {', repo_methods + '\n    suspend fun insertProject(project: Project): Long {')

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)
