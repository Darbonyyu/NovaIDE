import re

with open('./app/src/main/java/com/example/data/db/Daos.kt', 'r') as f:
    content = f.read()

dao_methods = """
    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): ChatMessage?
"""
content = content.replace('    suspend fun clearChatForProject(projectId: Long)', '    suspend fun clearChatForProject(projectId: Long)\n' + dao_methods)

with open('./app/src/main/java/com/example/data/db/Daos.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun deleteMessage(id: Long) {
        db.chatDao().deleteMessage(id)
    }

    suspend fun getMessageById(id: Long): ChatMessage? {
        return db.chatDao().getMessageById(id)
    }
"""
content = content.replace('    suspend fun clearChat(projectId: Long) {', repo_methods + '\n    suspend fun clearChat(projectId: Long) {')

with open('./app/src/main/java/com/example/data/repository/IdeRepository.kt', 'w') as f:
    f.write(content)

