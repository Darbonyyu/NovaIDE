package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.ApiProvider
import com.example.data.models.ProjectFile
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface

data class PaletteAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "FILE", "AI", "NAV", "SYSTEM"
    val icon: ImageVector,
    val onExecute: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandPaletteModal(
    onDismiss: () -> Unit,
    projectFiles: List<ProjectFile>,
    providers: List<ApiProvider>,
    onOpenFile: (ProjectFile) -> Unit,
    onSelectProvider: (ApiProvider) -> Unit,
    onToggleTerminal: () -> Unit,
    onToggleLivePreview: () -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onExportZip: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allActions = remember(projectFiles, providers, searchQuery) {
        val actions = mutableListOf<PaletteAction>()

        // System Actions
        actions.add(
            PaletteAction(
                id = "term",
                title = "Открыть Терминал",
                subtitle = "Переключить интегрированную консоль",
                category = "SYSTEM",
                icon = Icons.Default.Terminal,
                onExecute = { onToggleTerminal(); onDismiss() }
            )
        )
        actions.add(
            PaletteAction(
                id = "preview",
                title = "Живой Просмотр (Live Preview)",
                subtitle = "Запустить встроенный HTML/Markdown просмотрщик",
                category = "SYSTEM",
                icon = Icons.Default.PlayArrow,
                onExecute = { onToggleLivePreview(); onDismiss() }
            )
        )
        actions.add(
            PaletteAction(
                id = "zip",
                title = "Экспортировать проект в ZIP",
                subtitle = "Скачать полную файловую структуру",
                category = "SYSTEM",
                icon = Icons.Default.Download,
                onExecute = { onExportZip(); onDismiss() }
            )
        )
        actions.add(
            PaletteAction(
                id = "clear",
                title = "Очистить текущий Чат",
                subtitle = "Сбросить историю сообщений",
                category = "SYSTEM",
                icon = Icons.Default.DeleteSweep,
                onExecute = { onClearChat(); onDismiss() }
            )
        )

        // AI Prompt Presets
        actions.add(
            PaletteAction(
                id = "ai_refactor",
                title = "AI: Провести рефакторинг проекта",
                subtitle = "Запросить у ИИ проверку чистоты архитектуры",
                category = "AI",
                icon = Icons.Default.AutoAwesome,
                onExecute = { onSendMessage("Проведи полный рефакторинг открытого файла и предложи оптимизации."); onDismiss() }
            )
        )
        actions.add(
            PaletteAction(
                id = "ai_bugs",
                title = "AI: Найти потенциальные баги",
                subtitle = "Поиск утечек памяти и асинхронных ошибок",
                category = "AI",
                icon = Icons.Default.BugReport,
                onExecute = { onSendMessage("Найди баги и уязвимости в коде проекта."); onDismiss() }
            )
        )
        actions.add(
            PaletteAction(
                id = "ai_tests",
                title = "AI: Сгенерировать Unit-тесты",
                subtitle = "Создать тесты JUnit / Robolectric",
                category = "AI",
                icon = Icons.Default.Verified,
                onExecute = { onSendMessage("Сгенерируй покрытие Unit-тестами для этого модуля."); onDismiss() }
            )
        )

        // AI Providers
        providers.forEach { prov ->
            actions.add(
                PaletteAction(
                    id = "prov_${prov.id}",
                    title = "Модель: ${prov.name}",
                    subtitle = "Переключить генератор на ${prov.selectedModel}",
                    category = "AI",
                    icon = Icons.Default.Psychology,
                    onExecute = { onSelectProvider(prov); onDismiss() }
                )
            )
        }

        // File Jump Actions
        projectFiles.filter { !it.isFolder }.forEach { file ->
            actions.add(
                PaletteAction(
                    id = "file_${file.id}",
                    title = file.filename,
                    subtitle = file.path,
                    category = "FILE",
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    onExecute = { onOpenFile(file); onDismiss() }
                )
            )
        }

        if (searchQuery.isBlank()) actions else actions.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Палитра команд (Cmd+K / Ctrl+P)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentIndigo
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск файлов, команд ИИ, настроек...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentIndigo) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentIndigo,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allActions) { act ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { act.onExecute() }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = act.icon,
                                    contentDescription = null,
                                    tint = if (act.category == "AI") AccentPurple else AccentIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = act.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = act.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
