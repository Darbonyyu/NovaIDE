with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun getProjectById(id: Long): Project? {
        return db.projectDao().getProjectById(id)
    }

    suspend fun updateProject(project: Project) {
        db.projectDao().updateProject(project)
    }

    suspend fun createProject(name: String, description: String, language: String): Long {"""

content = content.replace('    suspend fun createProject(name: String, description: String, language: String): Long {', repo_methods)

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)
