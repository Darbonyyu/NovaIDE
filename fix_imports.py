import re
import os

files_to_check = [
    './app/src/main/java/com/example/ui/components/CodeEditor.kt',
    './app/src/main/java/com/example/ui/utils/SyntaxHighlighting.kt',
    './app/src/main/java/com/example/ui/components/CodeBlockCard.kt',
    './app/src/main/java/com/example/ui/components/CodeDiffDialog.kt',
    './app/src/main/java/com/example/ui/components/CommandPaletteModal.kt',
    './app/src/main/java/com/example/ui/screens/HistoryScreen.kt'
]

for path in files_to_check:
    if os.path.exists(path):
        with open(path, 'r') as f:
            content = f.read()
        if 'MaterialTheme.' in content and 'import androidx.compose.material3.MaterialTheme' not in content:
            content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.material3.MaterialTheme')
            with open(path, 'w') as f:
                f.write(content)
