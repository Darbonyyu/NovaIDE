package com.example.data.db

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}

@Dao
interface FileDao {
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isFolder DESC, filename ASC")
    fun getFilesForProject(projectId: Long): Flow<List<ProjectFile>>

    @Query("SELECT * FROM project_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): ProjectFile?

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: Long, path: String): ProjectFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFile>)

    @Update
    suspend fun updateFile(file: ProjectFile)

    @Query("DELETE FROM project_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND path LIKE :pathPrefix || '%'")
    suspend fun deleteFilesByPathPrefix(projectId: Long, pathPrefix: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getMessagesForProject(projectId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE projectId = :projectId")
    suspend fun clearChatForProject(projectId: Long)
}

@Dao
interface ApiProviderDao {
    @Query("SELECT * FROM api_providers ORDER BY id ASC")
    fun getAllProviders(): Flow<List<ApiProvider>>

    @Query("SELECT * FROM api_providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: Long): ApiProvider?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ApiProvider): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ApiProvider>)

    @Update
    suspend fun updateProvider(provider: ApiProvider)

    @Query("DELETE FROM api_providers WHERE id = :id")
    suspend fun deleteProviderById(id: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem): Long

    @Query("DELETE FROM history_items")
    suspend fun clearHistory()
}
