package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.IdeViewModel
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.DarkSurface

@Composable
fun SettingsScreen(viewModel: IdeViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var themeMode by remember(settings) { mutableStateOf(settings.themeMode) }
    var language by remember(settings) { mutableStateOf(settings.language) }
    var fontSize by remember(settings) { mutableStateOf(settings.fontSize.toFloat()) }
    var animationsEnabled by remember(settings) { mutableStateOf(settings.animationsEnabled) }
    var biometricsEnabled by remember(settings) { mutableStateOf(settings.biometricsEnabled) }

    var showClearConfirm by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importProjectZip(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = AccentIndigo)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (language == "RU") "Настройки AI IDE" else "AI IDE Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == "RU") "Параметры интерфейса, шрифтов, шифрования и бэкапа" else "Interface, fonts, encryption, and backup parameters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Appearance & Theme
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == "RU") "Внешний вид и Тема" else "Appearance & Theme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentIndigo
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "RU") "Тема оформления" else "Theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row {
                        FilterChip(
                            selected = themeMode == "DARK",
                            onClick = {
                                themeMode = "DARK"
                                viewModel.updateSettings(settings.copy(themeMode = "DARK"))
                            },
                            label = { Text(if (language == "RU") "Тёмная" else "Dark") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = themeMode == "LIGHT",
                            onClick = {
                                themeMode = "LIGHT"
                                viewModel.updateSettings(settings.copy(themeMode = "LIGHT"))
                            },
                            label = { Text(if (language == "RU") "Светлая" else "Light") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "RU") "Язык интерфейса" else "Interface Language", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row {
                        FilterChip(
                            selected = language == "RU",
                            onClick = {
                                language = "RU"
                                viewModel.updateSettings(settings.copy(language = "RU"))
                            },
                            label = { Text("RU") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = language == "EN",
                            onClick = {
                                language = "EN"
                                viewModel.updateSettings(settings.copy(language = "EN"))
                            },
                            label = { Text("EN") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section: Editor & Fonts
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == "RU") "Редактор и Шрифты" else "Editor & Fonts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentPurple
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (language == "RU") "Размер шрифта в редакторе: ${fontSize.toInt()} sp" else "Editor font size: ${fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    onValueChangeFinished = {
                        viewModel.updateSettings(settings.copy(fontSize = fontSize.toInt()))
                    },
                    valueRange = 12f..22f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = AccentIndigo, activeTrackColor = AccentIndigo)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "RU") "Плавные анимации UI" else "Smooth UI animations", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = animationsEnabled,
                        onCheckedChange = {
                            animationsEnabled = it
                            viewModel.updateSettings(settings.copy(animationsEnabled = it))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section: Security & Backup
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == "RU") "Безопасность и Резервное Копирование" else "Security & Backup",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentIndigo
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "RU") "Защита биометрией (Face / Fingerprint)" else "Biometric protection (Face / Fingerprint)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = biometricsEnabled,
                        onCheckedChange = {
                            biometricsEnabled = it
                            viewModel.updateSettings(settings.copy(biometricsEnabled = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.exportProjectZip(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "RU") "Экспорт ZIP" else "Export ZIP")
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch("application/zip") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "RU") "Импорт ZIP" else "Import ZIP")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Danger Zone: Delete All Data
        Button(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.2f), contentColor = AccentOrange),
            border = BorderStroke(1.dp, AccentOrange),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (language == "RU") "Удалить все локальные данные" else "Delete all local data", fontWeight = FontWeight.Bold)
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(if (language == "RU") "Очистить все данные?" else "Clear all data?") },
            text = { Text(if (language == "RU") "Это действие удалит все проекты, ключи API и историю сообщений." else "This action will delete all projects, API keys and message history.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White)
                ) {
                    Text(if (language == "RU") "Удалить" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(if (language == "RU") "Отмена" else "Cancel") }
            }
        )
    }
}
