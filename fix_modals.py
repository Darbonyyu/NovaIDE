with open('./app/src/main/java/com/example/ui/components/workspace/WorkspaceModals.kt', 'r') as f:
    content = f.read()

replacement = """        confirmButton = {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
                androidx.compose.foundation.layout.Row {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Button(onClick = onSave) {
                        Text("Сохранить")
                    }
                }
            }
        },
        dismissButton = {}"""

import re
content = re.sub(r'        confirmButton = \{[\s\S]*?dismissButton = \{[\s\S]*?\}\n    \)', replacement + '\n    )', content)

# Check if Modifier.fillMaxWidth is imported
if 'import androidx.compose.foundation.layout.fillMaxWidth' not in content:
    content = content.replace('import androidx.compose.foundation.layout.Column', 'import androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.fillMaxWidth')

with open('./app/src/main/java/com/example/ui/components/workspace/WorkspaceModals.kt', 'w') as f:
    f.write(content)
