content = """package com.example.ui.components.workspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.Project

@Composable
fun CreateFolderDialog(
    folderName: String,
    onFolderNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать папку", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Введите название папки:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = onFolderNameChange,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onCreate) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun CreateFileDialog(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать новый файл", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Введите название файла (например: AuthService.kt, index.html, config.json):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onCreate) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun ProjectSettingsDialog(
    project: Project,
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки проекта", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Переименовать проект:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Button(onClick = onSave) {
                        Text("Сохранить")
                    }
                }
            }
        },
        dismissButton = {}
    )
}
"""
with open('./app/src/main/java/com/example/ui/components/workspace/WorkspaceModals.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    screen_content = f.read()

screen_content = screen_content.replace('viewModel.updateProject(proj.copy(name = renameProjectText))', 'viewModel.renameProject(proj.id, renameProjectText)')
with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(screen_content)

