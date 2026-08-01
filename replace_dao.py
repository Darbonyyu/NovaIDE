import re

with open('./app/src/main/java/com/example/data/db/Daos.kt', 'r') as f:
    content = f.read()

replacement = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("UPDATE chat_messages SET content = :content, codeBlocksJson = :codeBlocksJson WHERE id = :id")
    suspend fun updateMessage(id: Long, content: String, codeBlocksJson: String)

    @Query("DELETE FROM chat_messages"""

pattern = r"    @Insert\(onConflict = OnConflictStrategy\.REPLACE\)\n    suspend fun insertMessage\(message: ChatMessage\): Long\n\n    @Query\(\"DELETE FROM chat_messages"

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('./app/src/main/java/com/example/data/db/Daos.kt', 'w') as f:
    f.write(new_content)
