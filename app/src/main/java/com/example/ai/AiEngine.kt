package com.example.ai

import com.example.BuildConfig
import com.example.data.models.ApiProvider
import com.example.data.models.CodeBlock
import com.example.data.models.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class AiResponse(
    val fullText: String,
    val codeBlocks: List<CodeBlock>,
    val explanationText: String
)

object AiEngine {

    suspend fun testConnection(provider: ApiProvider): Triple<Boolean, String, Int> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val apiKey = provider.apiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
            if (apiKey.isBlank() && provider.type != "OLLAMA") {
                return@withContext Triple(false, "Требуется API ключ", 0)
            }

            if (provider.type == "GEMINI") {
                val base = provider.baseUrl.trimEnd('/')
                val urlStr = if (base.endsWith("/v1beta") || base.endsWith("/v1")) {
                    "$base/models/${provider.selectedModel.ifBlank { "gemini-1.5-flash" }}:generateContent?key=$apiKey"
                } else {
                    "$base/v1beta/models/${provider.selectedModel.ifBlank { "gemini-1.5-flash" }}:generateContent?key=$apiKey"
                }
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "Hello"))
                            })
                        })
                    })
                }

                OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
                val code = conn.responseCode
                val elapsed = (System.currentTimeMillis() - startTime).toInt()
                if (code in 200..299) {
                    return@withContext Triple(true, "$code OK", elapsed)
                } else {
                    return@withContext Triple(false, "HTTP Ошибка $code", elapsed)
                }
            } else {
                // OpenAI Compatible
                val testUrl = if (provider.type == "OLLAMA") {
                    URL("${provider.baseUrl.trimEnd('/')}/tags")
                } else {
                    URL("${provider.baseUrl.trimEnd('/')}/models")
                }
                val conn = testUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json")
                if (apiKey.isNotBlank()) {
                    conn.setRequestProperty("Authorization", "Bearer $apiKey")
                }
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val code = conn.responseCode
                val elapsed = (System.currentTimeMillis() - startTime).toInt()
                if (code in 200..299 || code == 401) {
                    if (code == 401) return@withContext Triple(false, "401 Неавторизован", elapsed)
                    return@withContext Triple(true, "$code OK", elapsed)
                } else {
                    return@withContext Triple(false, "HTTP Ошибка $code", elapsed)
                }
            }
        } catch (e: Exception) {
            val elapsed = (System.currentTimeMillis() - startTime).toInt()
            return@withContext Triple(false, "Ошибка соединения: ${e.message}", elapsed)
        }
    }

    fun generateAiResponseStream(
        prompt: String,
        provider: ApiProvider,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        specificCodeToModify: String? = null,
        attachedFiles: List<String> = emptyList()
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        val apiKey = provider.apiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (apiKey.isBlank() && provider.type != "OLLAMA") {
            throw Exception("API ключ не настроен.")
        }

        if (provider.type == "GEMINI") {
            streamGeminiRestApi(apiKey, provider, prompt, projectFiles, activeFile, specificCodeToModify, attachedFiles).collect { emit(it) }
        } else {
            streamOpenAiCompatibleApi(apiKey, provider, prompt, projectFiles, activeFile, specificCodeToModify, attachedFiles).collect { emit(it) }
        }
    }

    private fun streamGeminiRestApi(
        apiKey: String,
        provider: ApiProvider,
        prompt: String,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        codeToModify: String?,
        attachedFiles: List<String> = emptyList()
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        val base = provider.baseUrl.trimEnd('/')
        val urlStr = if (base.endsWith("/v1beta") || base.endsWith("/v1")) {
            "$base/models/${provider.selectedModel.ifBlank { "gemini-1.5-flash" }}:streamGenerateContent?alt=sse&key=$apiKey"
        } else {
            "$base/v1beta/models/${provider.selectedModel.ifBlank { "gemini-1.5-flash" }}:streamGenerateContent?alt=sse&key=$apiKey"
        }
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val contextPrompt = buildContextPrompt(prompt, projectFiles, activeFile, codeToModify, attachedFiles)

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", contextPrompt))
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        val code = conn.responseCode
        if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith("data: ")) {
                        try {
                            val json = JSONObject(line!!.substring(6))
                            val candidates = json.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val text = candidates.getJSONObject(0)
                                    .optJSONObject("content")
                                    ?.optJSONArray("parts")
                                    ?.optJSONObject(0)
                                    ?.optString("text") ?: ""
                                if (text.isNotEmpty()) emit(text)
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
        } else {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
            throw Exception("Ошибка API ($code): $err")
        }
    }

    private fun streamOpenAiCompatibleApi(
        apiKey: String,
        provider: ApiProvider,
        prompt: String,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        codeToModify: String?,
        attachedFiles: List<String> = emptyList()
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        val url = URL("${provider.baseUrl.trimEnd('/')}/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (apiKey.isNotBlank()) {
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
        }
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val contextPrompt = buildContextPrompt(prompt, projectFiles, activeFile, codeToModify, attachedFiles)

        val jsonBody = JSONObject().apply {
            put("model", provider.selectedModel)
            put("stream", true)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an expert AI IDE assistant.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", contextPrompt)
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        val code = conn.responseCode
        if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith("data: ") && !line!!.contains("[DONE]")) {
                        try {
                            val json = JSONObject(line!!.substring(6))
                            val text = json.optJSONArray("choices")
                                ?.optJSONObject(0)
                                ?.optJSONObject("delta")
                                ?.optString("content") ?: ""
                            if (text.isNotEmpty()) emit(text)
                        } catch (e: Exception) {}
                    }
                }
            }
        } else {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
            throw Exception("Ошибка API ($code): $err")
        }
    }

    suspend fun generateAiResponse(
        prompt: String,
        provider: ApiProvider,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        specificCodeToModify: String? = null
    ): AiResponse = withContext(Dispatchers.IO) {
        val apiKey = provider.apiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (apiKey.isBlank() && provider.type != "OLLAMA") {
            throw Exception("API ключ не настроен. Перейдите в настройки и добавьте ключ.")
        }

        val apiResult = if (provider.type == "GEMINI") {
            callGeminiRestApi(apiKey, provider, prompt, projectFiles, activeFile, specificCodeToModify)
        } else {
            callOpenAiCompatibleApi(apiKey, provider, prompt, projectFiles, activeFile, specificCodeToModify)
        }
        
        if (apiResult.isBlank()) {
            throw Exception("Пустой ответ от сервера.")
        }

        return@withContext parseResponseToAiResponse(apiResult)
    }

    private fun callGeminiRestApi(
        apiKey: String,
        provider: ApiProvider,
        prompt: String,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        codeToModify: String?
    ): String {
        val base = provider.baseUrl.trimEnd('/')
        val urlStr = if (base.endsWith("/v1beta") || base.endsWith("/v1")) {
            "$base/models/${provider.selectedModel.ifBlank { "gemini-1.5-flash" }}:generateContent?key=$apiKey"
        } else {
            "$base/v1beta/models/${provider.selectedModel.ifBlank { "gemini-1.5-flash" }}:generateContent?key=$apiKey"
        }
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val contextPrompt = buildContextPrompt(prompt, projectFiles, activeFile, codeToModify)

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", contextPrompt))
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        val code = conn.responseCode
        if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                val respJson = JSONObject(sb.toString())
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "")
                    }
                }
            }
        } else {
            val errorStream = conn.errorStream
            if (errorStream != null) {
                BufferedReader(InputStreamReader(errorStream)).use { reader ->
                    val err = reader.readText()
                    throw Exception("Ошибка API ($code): $err")
                }
            }
            throw Exception("Ошибка API ($code)")
        }
        return ""
    }

    private fun callOpenAiCompatibleApi(
        apiKey: String,
        provider: ApiProvider,
        prompt: String,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        codeToModify: String?
    ): String {
        val url = URL("${provider.baseUrl.trimEnd('/')}/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (apiKey.isNotBlank()) {
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
        }
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val contextPrompt = buildContextPrompt(prompt, projectFiles, activeFile, codeToModify)

        val jsonBody = JSONObject().apply {
            put("model", provider.selectedModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an expert AI IDE assistant.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", contextPrompt)
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
        val code = conn.responseCode
        if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                val respJson = JSONObject(sb.toString())
                val choices = respJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    return message?.optString("content", "") ?: ""
                }
            }
        } else {
            val errorStream = conn.errorStream
            if (errorStream != null) {
                BufferedReader(InputStreamReader(errorStream)).use { reader ->
                    val err = reader.readText()
                    throw Exception("Ошибка API ($code): $err")
                }
            }
            throw Exception("Ошибка API ($code)")
        }
        return ""
    }

    private fun buildContextPrompt(
        prompt: String,
        projectFiles: List<ProjectFile>,
        activeFile: ProjectFile?,
        codeToModify: String?,
        attachedFiles: List<String> = emptyList()
    ): String {
        val contextPrompt = StringBuilder()
        if (attachedFiles.isNotEmpty()) {
            contextPrompt.append("ATTACHED FILES FOR CONTEXT:\n")
            attachedFiles.forEach { fName ->
                val matched = projectFiles.find { it.filename == fName || it.path == fName }
                if (matched != null) {
                    contextPrompt.append("File (${matched.path}):\n```${matched.extension}\n${matched.content}\n```\n\n")
                } else {
                    contextPrompt.append("File reference: $fName\n\n")
                }
            }
        }
        if (projectFiles.isNotEmpty()) {
            contextPrompt.append("Project files:\n")
            projectFiles.take(5).forEach {
                if (!it.isFolder) {
                    contextPrompt.append("- ${it.path} (${it.content.take(200).replace("\n", " ")})\n")
                }
            }
        }
        if (activeFile != null) {
            contextPrompt.append("\nActive File (${activeFile.filename}):\n```${activeFile.extension}\n${activeFile.content}\n```\n")
        }
        if (!codeToModify.isNullOrBlank()) {
            contextPrompt.append("\nCode to modify:\n```\n$codeToModify\n```\n")
        }
        contextPrompt.append("\nUser Request: $prompt")
        return contextPrompt.toString()
    }

    fun parseResponseToAiResponse(text: String): AiResponse {
        val codeBlocks = mutableListOf<CodeBlock>()
        val regex = Regex("```([a-zA-Z0-9_+-]*)\n([\\s\\S]*?)```")
        val matches = regex.findAll(text)

        val cleanExplanation = StringBuilder()
        var lastIndex = 0

        for (match in matches) {
            val lang = match.groupValues[1].ifEmpty { "kotlin" }
            val code = match.groupValues[2].trim()
            val blockId = UUID.randomUUID().toString()

            codeBlocks.add(
                CodeBlock(
                    id = blockId,
                    code = code,
                    language = lang,
                    explanation = "Блок кода #$lang"
                )
            )

            val textBefore = text.substring(lastIndex, match.range.first)
            cleanExplanation.append(textBefore)
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            cleanExplanation.append(text.substring(lastIndex))
        }

        val explanationText = cleanExplanation.toString().trim()
        return AiResponse(
            fullText = text,
            codeBlocks = codeBlocks,
            explanationText = explanationText.ifEmpty { "Результат генерации ИИ:" }
        )
    }
}
