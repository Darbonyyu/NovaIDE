package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val language: String = "Kotlin",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)

@Entity(tableName = "project_files")
data class ProjectFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val path: String, // e.g. "src/main/Main.kt"
    val filename: String, // e.g. "Main.kt"
    val extension: String, // e.g. "kt", "json", "html"
    val content: String,
    val isFolder: Boolean = false,
    val parentPath: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val sender: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedFilesJson: String = "[]", // Serialized JSON array of file names/URIs
    val codeBlocksJson: String = "[]" // Serialized JSON array of code blocks
)

data class CodeBlock(
    val id: String,
    val code: String,
    val language: String,
    val isApplied: Boolean = false,
    val originalCode: String = "",
    val explanation: String = ""
)

@Entity(tableName = "api_providers")
data class ApiProvider(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Google Gemini", "OpenAI", "DeepSeek", "Ollama"
    val type: String, // "GEMINI", "OPENAI", "CLAUDE", "DEEPSEEK", "GROK", "OLLAMA", "CUSTOM"
    val baseUrl: String = "",
    val apiKey: String = "",
    val selectedModel: String = "",
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val latencyMs: Long = 0,
    val lastPingStatus: String = "OK"
)

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val prompt: String,
    val providerName: String,
    val modelName: String,
    val codeSnippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppSettings(
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM
    val language: String = "RU", // RU, EN
    val fontSize: Int = 14, // 12..22
    val fontFamily: String = "JetBrains Mono",
    val animationsEnabled: Boolean = true,
    val biometricsEnabled: Boolean = false,
    val autoSave: Boolean = true,
    val activeProjectId: Long = 1,
    val activeProviderId: Long = 1
)
