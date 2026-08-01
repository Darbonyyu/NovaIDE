package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

import android.content.SharedPreferences

class IdeRepository(private val db: AppDatabase, private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_ide_settings", Context.MODE_PRIVATE)


    val allProjects: Flow<List<Project>> = db.projectDao().getAllProjects()
    val allProviders: Flow<List<ApiProvider>> = db.apiProviderDao().getAllProviders()
    val allHistory: Flow<List<HistoryItem>> = db.historyDao().getAllHistory()

    private val _settings = MutableStateFlow(loadSettings())
    
    private fun loadSettings(): AppSettings {
        return AppSettings(
            themeMode = prefs.getString("themeMode", "DARK") ?: "DARK",
            language = prefs.getString("language", "RU") ?: "RU",
            fontSize = prefs.getInt("fontSize", 14),
            animationsEnabled = prefs.getBoolean("animationsEnabled", true),
            biometricsEnabled = prefs.getBoolean("biometricsEnabled", false)
        )
    }

    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun getFilesForProject(projectId: Long): Flow<List<ProjectFile>> {
        return db.fileDao().getFilesForProject(projectId)
    }

    fun getMessagesForProject(projectId: Long): Flow<List<ChatMessage>> {
        return db.chatDao().getMessagesForProject(projectId)
    }

    suspend fun getFileByPath(projectId: Long, path: String): ProjectFile? {
        return db.fileDao().getFileByPath(projectId, path)
    }


    suspend fun getProjectById(id: Long): Project? {
        return db.projectDao().getProjectById(id)
    }

    suspend fun createProject(name: String, description: String, language: String): Long {
        val proj = Project(
            name = name,
            description = description,
            language = language,
            updatedAt = System.currentTimeMillis()
        )
        val projId = db.projectDao().insertProject(proj)
        
        // Populate default starter files based on language/template
        val defaultFiles = createDefaultFilesForProject(projId, name, language)
        db.fileDao().insertFiles(defaultFiles)

        // Add initial greeting message in chat
        val initialChat = ChatMessage(
            projectId = projId,
            sender = "assistant",
            content = "Привет! Я твой ИИ-ассистент для проекта **$name**.\n\nЯ помогу написать код, рефакторить, искать ошибки, создавать файлы и запускать тесты. Чем можем заняться?",
            timestamp = System.currentTimeMillis()
        )
        db.chatDao().insertMessage(initialChat)

        return projId
    }

    suspend fun updateProject(project: Project) {
        db.projectDao().updateProject(project)
    }

    suspend fun deleteProject(id: Long) {
        db.projectDao().deleteProjectById(id)
    }




    suspend fun saveFile(file: ProjectFile): Long {
        return db.fileDao().insertFile(file)
    }

    suspend fun updateFileContent(fileId: Long, newContent: String) {
        val file = db.fileDao().getFileById(fileId)
        if (file != null) {
            db.fileDao().updateFile(file.copy(content = newContent, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteFile(fileId: Long) {
        val file = db.fileDao().getFileById(fileId)
        if (file != null) {
            if (file.isFolder) {
                db.fileDao().deleteFilesByPathPrefix(file.projectId, file.path)
            }
            db.fileDao().deleteFileById(fileId)
        }
    }

    suspend fun updateChatMessage(messageId: Long, content: String, codeBlocksJson: String) {
        db.chatDao().updateMessage(messageId, content, codeBlocksJson)
    }

    suspend fun insertChatMessage(message: ChatMessage): Long {
        return return db.chatDao().insertMessage(message)
    }


    suspend fun deleteMessage(id: Long) {
        db.chatDao().deleteMessage(id)
    }

    suspend fun getMessageById(id: Long): ChatMessage? {
        return db.chatDao().getMessageById(id)
    }

    suspend fun clearChat(projectId: Long) {
        db.chatDao().clearChatForProject(projectId)
    }

    suspend fun saveProvider(provider: ApiProvider): Long {
        return db.apiProviderDao().insertProvider(provider)
    }

    suspend fun deleteProvider(id: Long) {
        db.apiProviderDao().deleteProviderById(id)
    }

    suspend fun addHistory(item: HistoryItem) {
        db.historyDao().insertHistory(item)
    }

    suspend fun clearHistory() {
        db.historyDao().clearHistory()
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("themeMode", newSettings.themeMode)
            putString("language", newSettings.language)
            putInt("fontSize", newSettings.fontSize)
            putBoolean("animationsEnabled", newSettings.animationsEnabled)
            putBoolean("biometricsEnabled", newSettings.biometricsEnabled)
            apply()
        }
    }

    suspend fun seedInitialDataIfNeeded() {
        val projects = db.projectDao().getAllProjects().first()
        if (projects.isEmpty()) {
            val proj1 = createProject("AI Assistant Mobile", "Jetpack Compose Android IDE App", "Kotlin")
            val proj2 = createProject("React Live Web", "Web Dashboard in React & Tailwind", "TypeScript")
            val proj3 = createProject("Python Microservice", "FastAPI AI agent backend", "Python")

            _settings.value = _settings.value.copy(activeProjectId = proj1)
        }

        val providers = db.apiProviderDao().getAllProviders().first()
        if (providers.isEmpty()) {
            val defaultProviders = listOf(
                ApiProvider(
                    name = "Google Gemini",
                    type = "GEMINI",
                    baseUrl = "https://generativelanguage.googleapis.com",
                    apiKey = "",
                    selectedModel = "gemini-1.5-flash",
                    isEnabled = true,
                    latencyMs = 120,
                    lastPingStatus = "200 OK"
                ),
                ApiProvider(
                    name = "OpenAI",
                    type = "OPENAI",
                    baseUrl = "https://api.openai.com/v1",
                    apiKey = "",
                    selectedModel = "gpt-4o",
                    isEnabled = true,
                    latencyMs = 180,
                    lastPingStatus = "Ready"
                ),
                ApiProvider(
                    name = "Anthropic Claude",
                    type = "CLAUDE",
                    baseUrl = "https://api.anthropic.com/v1",
                    apiKey = "",
                    selectedModel = "claude-3-5-sonnet-20241022",
                    isEnabled = true,
                    latencyMs = 150,
                    lastPingStatus = "Ready"
                ),
                ApiProvider(
                    name = "DeepSeek AI",
                    type = "DEEPSEEK",
                    baseUrl = "https://api.deepseek.com/v1",
                    apiKey = "",
                    selectedModel = "deepseek-coder",
                    isEnabled = true,
                    latencyMs = 95,
                    lastPingStatus = "200 OK"
                ),
                ApiProvider(
                    name = "xAI Grok",
                    type = "GROK",
                    baseUrl = "https://api.x.ai/v1",
                    apiKey = "",
                    selectedModel = "grok-beta",
                    isEnabled = true,
                    latencyMs = 210,
                    lastPingStatus = "Ready"
                ),
                ApiProvider(
                    name = "Ollama Local (Offline)",
                    type = "OLLAMA",
                    baseUrl = "http://localhost:11434/v1",
                    apiKey = "local",
                    selectedModel = "llama3:latest",
                    isEnabled = true,
                    isCustom = true,
                    latencyMs = 15,
                    lastPingStatus = "Localhost OK"
                ),
                ApiProvider(
                    name = "OpenRouter",
                    type = "OPENROUTER",
                    baseUrl = "https://openrouter.ai/api/v1",
                    apiKey = "",
                    selectedModel = "auto",
                    isEnabled = true,
                    latencyMs = 140,
                    lastPingStatus = "Ready"
                )
            )
            db.apiProviderDao().insertProviders(defaultProviders)
        }
    }

    private fun createDefaultFilesForProject(projectId: Long, projName: String, language: String): List<ProjectFile> {
        val files = mutableListOf<ProjectFile>()
        when (language.lowercase()) {
            "kotlin", "android" -> {
                files.add(ProjectFile(projectId = projectId, path = "src", filename = "src", extension = "", content = "", isFolder = true))
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "src/Main.kt",
                    filename = "Main.kt",
                    extension = "kt",
                    content = """
                        package com.app.ide
                        
                        import kotlinx.coroutines.*
                        
                        /**
                         * $projName - Main Entry Point
                         * High speed AI-assisted execution engine
                         */
                        fun main() = runBlocking {
                            println("🚀 Starting $projName AI IDE Engine...")
                            val model = "Gemini 1.5 Flash"
                            println("Connecting to model: ${'$'}model")
                            
                            val result = runAiPipeline("Generate Compose UI Scaffold")
                            println("Result: ${'$'}result")
                        }

                        suspend fun runAiPipeline(prompt: String): String {
                            delay(300)
                            return "✓ Pipeline successfully generated code for: ${'$'}prompt"
                        }
                    """.trimIndent(),
                    parentPath = "src"
                ))
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "src/UiComponents.kt",
                    filename = "UiComponents.kt",
                    extension = "kt",
                    content = """
                        package com.app.ide
                        
                        // Compose Modern UI Components
                        class ButtonStyle {
                            val cornerRadius: Int = 16
                            val elevationDp: Int = 4
                            val isGlassmorphic: Boolean = true
                        }
                        
                        fun renderModernCard(title: String) {
                            println("Rendering card: ${'$'}title with 16dp rounded borders")
                        }
                    """.trimIndent(),
                    parentPath = "src"
                ))
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "README.md",
                    filename = "README.md",
                    extension = "md",
                    content = """
                        # $projName

                        Minimalist AI IDE mobile application.

                        ## Features
                        - Multi-provider AI Integration (Gemini, DeepSeek, Claude, Ollama)
                        - Real-time syntax highlighting & live web preview
                        - VS Code inspired Command Palette
                        - Direct multi-file code replacement
                    """.trimIndent()
                ))
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "config.json",
                    filename = "config.json",
                    extension = "json",
                    content = """
                        {
                          "projectName": "$projName",
                          "version": "1.0.0",
                          "aiMode": "agent",
                          "contextWindowTokens": 128000,
                          "features": ["live_preview", "terminal", "diff_view"]
                        }
                    """.trimIndent()
                ))
            }
            "typescript", "javascript", "react", "html" -> {
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "index.html",
                    filename = "index.html",
                    extension = "html",
                    content = """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>$projName - Live Web App</title>
                            <style>
                                body {
                                    background: #0D0E15;
                                    color: #F3F4F6;
                                    font-family: system-ui, -apple-system, sans-serif;
                                    display: flex;
                                    flex-direction: column;
                                    align-items: center;
                                    justify-content: center;
                                    min-height: 100vh;
                                    margin: 0;
                                }
                                .card {
                                    background: #171822;
                                    border: 1px solid rgba(255, 255, 255, 0.1);
                                    border-radius: 16px;
                                    padding: 24px;
                                    box-shadow: 0 10px 30px rgba(0,0,0,0.5);
                                    text-align: center;
                                    max-width: 400px;
                                }
                                .badge {
                                    background: linear-gradient(135deg, #00E5FF, #7C4DFF);
                                    color: #000;
                                    font-weight: bold;
                                    padding: 4px 12px;
                                    border-radius: 20px;
                                    font-size: 12px;
                                }
                                button {
                                    background: #00E5FF;
                                    color: #000;
                                    border: none;
                                    padding: 10px 20px;
                                    border-radius: 10px;
                                    font-weight: bold;
                                    cursor: pointer;
                                    margin-top: 16px;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="card">
                                <span class="badge">AI IDE PREVIEW</span>
                                <h2>$projName</h2>
                                <p>This is a live HTML/CSS preview compiled inside Mobile AI IDE.</p>
                                <button onclick="alert('Connected to AI IDE Agent!')">Click Me</button>
                            </div>
                        </body>
                        </html>
                    """.trimIndent()
                ))
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "app.ts",
                    filename = "app.ts",
                    extension = "ts",
                    content = """
                        interface AppState {
                            activeModel: string;
                            requestsCount: number;
                        }

                        const state: AppState = {
                            activeModel: "Gemini 1.5 Flash",
                            requestsCount: 42
                        };

                        console.log("App loaded with state:", state);
                    """.trimIndent()
                ))
            }
            else -> {
                files.add(ProjectFile(
                    projectId = projectId,
                    path = "main.py",
                    filename = "main.py",
                    extension = "py",
                    content = """
                        # $projName AI Script
                        import time

                        def run_ai_task(task_name: str):
                            print(f"🤖 Executing AI Task: {task_name}")
                            time.sleep(0.5)
                            return {"status": "success", "task": task_name}

                        if __name__ == "__main__":
                            res = run_ai_task("Code Optimization")
                            print("Output:", res)
                    """.trimIndent()
                ))
            }
        }
        return files
    }
}
