import os

files = [
    './app/src/main/java/com/example/ui/components/CodeBlockCard.kt',
    './app/src/main/java/com/example/ui/screens/ChatScreen.kt',
    './app/src/main/java/com/example/ui/components/CommandPaletteModal.kt',
    './app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt'
]

for fpath in files:
    with open(fpath, 'r') as f:
        content = f.read()
    
    if 'automirrored' not in content:
        content = content.replace('import androidx.compose.material.icons.filled.', 'import androidx.compose.material.icons.automirrored.filled.*\nimport androidx.compose.material.icons.filled.')
        with open(fpath, 'w') as f:
            f.write(content)
