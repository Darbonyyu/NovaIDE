import re

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'class CodeVisualTransformation : VisualTransformation',
    'class CodeVisualTransformation(val onSurfaceColor: Color) : VisualTransformation'
)
content = content.replace(
    'buildAnnotatedCodeLine(line, "kotlin", MaterialTheme.colorScheme.onSurface)',
    'buildAnnotatedCodeLine(line, "kotlin", onSurfaceColor)'
)
content = content.replace(
    'visualTransformation = CodeVisualTransformation(),',
    'visualTransformation = CodeVisualTransformation(MaterialTheme.colorScheme.onSurface),'
)

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'w') as f:
    f.write(content)
