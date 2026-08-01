package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ApiProvider
import com.example.ui.IdeViewModel
import com.example.ui.theme.*

@Composable
fun ApiProvidersScreen(viewModel: IdeViewModel) {
    val providers by viewModel.allProviders.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingProvider by remember { mutableStateOf<ApiProvider?>(null) }

    var nameInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("OPENAI") }
    var urlInput by remember { mutableStateOf("") }
    var keyInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }

    val presetTypes = listOf("GEMINI", "OPENAI", "CLAUDE", "DEEPSEEK", "GROK", "OLLAMA", "OPENROUTER", "CUSTOM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        // Top Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Управление AI API Провайдерами",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Подключайте любую модель (OpenAI, Claude, Gemini, DeepSeek, Grok, Ollama)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        editingProvider = null
                        nameInput = "Новый API Endpoint"
                        typeInput = "CUSTOM"
                        urlInput = "https://api.openai.com/v1"
                        keyInput = ""
                        modelInput = "gpt-4o"
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Providers List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(providers) { provider ->
                val isSelected = provider.id == selectedProvider?.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 0.5.dp,
                        if (isSelected) AccentIndigo else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (provider.lastPingStatus.contains("OK") || provider.lastPingStatus.contains("Ready")) AccentEmerald else AccentOrange)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = provider.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AccentIndigo,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = "АКТИВЕН",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { viewModel.testProviderConnection(provider) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = "Test Ping", tint = AccentOrange)
                                }
                                IconButton(
                                    onClick = {
                                        editingProvider = provider
                                        nameInput = provider.name
                                        typeInput = provider.type
                                        urlInput = provider.baseUrl
                                        keyInput = provider.apiKey
                                        modelInput = provider.selectedModel
                                        showEditDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentIndigo)
                                }
                                if (provider.isCustom) {
                                    IconButton(
                                        onClick = { viewModel.deleteProvider(provider.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Модель: ${provider.selectedModel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Статус: ${provider.lastPingStatus}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (provider.lastPingStatus.contains("OK")) AccentEmerald else AccentOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!isSelected) {
                            Button(
                                onClick = { viewModel.selectProvider(provider) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Переключиться на эту модель", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Edit / Add API Provider
    if (showEditDialog) {
        var keyVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingProvider != null) "Редактировать API" else "Добавить новый API Endpoint",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Название провайдера") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = { Text("Имя модели (e.g. gpt-4o, claude-3-5, gemini-1.5-flash)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Base Endpoint URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("API Key (шифруется локально)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Key Visibility"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prov = editingProvider?.copy(
                            name = nameInput,
                            type = typeInput,
                            baseUrl = urlInput,
                            apiKey = keyInput,
                            selectedModel = modelInput
                        ) ?: ApiProvider(
                            name = nameInput,
                            type = typeInput,
                            baseUrl = urlInput,
                            apiKey = keyInput,
                            selectedModel = modelInput,
                            isCustom = true
                        )
                        viewModel.saveProvider(prov)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo, contentColor = Color.Black)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Отмена") }
            }
        )
    }
}
