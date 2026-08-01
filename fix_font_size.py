import re

# Update CodeEditor.kt
with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun CodeEditor(',
    'fun CodeEditor(fontSize: Int = 13, '
)
content = content.replace('fontSize = 13.sp', 'fontSize = fontSize.sp')
with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'w') as f:
    f.write(content)

# Update WorkspaceEditor.kt
with open('./app/src/main/java/com/example/ui/components/workspace/WorkspaceEditor.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun WorkspaceEditor(',
    'fun WorkspaceEditor(fontSize: Int = 13, '
)
content = content.replace('fontSize = 12.sp', 'fontSize = fontSize.sp')
content = content.replace(
    'CodeEditor(\n                code = editorTextState,\n                onCodeChange = onCodeChange,',
    'CodeEditor(\n                fontSize = fontSize,\n                code = editorTextState,\n                onCodeChange = onCodeChange,'
)
with open('./app/src/main/java/com/example/ui/components/workspace/WorkspaceEditor.kt', 'w') as f:
    f.write(content)

# Update ProjectsWorkspaceScreen.kt
with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

if 'val settings by viewModel.settings.collectAsState()' not in content:
    content = content.replace(
        'val openTabs by viewModel.openTabs.collectAsState()',
        'val openTabs by viewModel.openTabs.collectAsState()\n    val settings by viewModel.settings.collectAsState()'
    )

content = content.replace(
    'com.example.ui.components.workspace.WorkspaceEditor(',
    'com.example.ui.components.workspace.WorkspaceEditor(\n                    fontSize = settings.fontSize,'
)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)

