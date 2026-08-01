import re

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'r') as f:
    content = f.read()

content = content.replace('buildAnnotatedCodeLine(line, "kotlin")', 'buildAnnotatedCodeLine(line, "kotlin", MaterialTheme.colorScheme.onSurface)')
with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'w') as f:
    f.write(content)

with open('./app/src/main/java/com/example/ui/components/CodeBlockCard.kt', 'r') as f:
    content = f.read()

content = content.replace('buildAnnotatedCodeLine(line, codeBlock.language)', 'buildAnnotatedCodeLine(line, codeBlock.language, MaterialTheme.colorScheme.onSurface)')
with open('./app/src/main/java/com/example/ui/components/CodeBlockCard.kt', 'w') as f:
    f.write(content)

