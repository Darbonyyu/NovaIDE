import re

with open('./app/src/main/java/com/example/ui/components/workspace/TerminalView.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun TerminalView(',
    'fun TerminalView(fontSize: Int = 11, '
)
content = content.replace('fontSize = 11.sp', 'fontSize = fontSize.sp')
content = content.replace('fontSize = 12.sp', 'fontSize = (fontSize + 1).sp')
with open('./app/src/main/java/com/example/ui/components/workspace/TerminalView.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'com.example.ui.components.workspace.TerminalView(',
    'com.example.ui.components.workspace.TerminalView(\n            fontSize = settings.fontSize - 2,'
)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
