import re

with open('./app/src/main/java/com/example/ui/utils/SyntaxHighlighting.kt', 'r') as f:
    content = f.read()

if 'import androidx.compose.material3.MaterialTheme' not in content:
    content = content.replace('import androidx.compose.ui.graphics.Color', 'import androidx.compose.ui.graphics.Color\nimport androidx.compose.material3.MaterialTheme')
    with open('./app/src/main/java/com/example/ui/utils/SyntaxHighlighting.kt', 'w') as f:
        f.write(content)
