import re

with open('./app/src/main/java/com/example/ui/utils/SyntaxHighlighting.kt', 'r') as f:
    content = f.read()

content = content.replace('fun buildAnnotatedCodeLine(line: String, lang: String): AnnotatedString', 'fun buildAnnotatedCodeLine(line: String, lang: String, defaultColor: Color = Color.White): AnnotatedString')
content = content.replace('MaterialTheme.colorScheme.onSurface', 'defaultColor')

with open('./app/src/main/java/com/example/ui/utils/SyntaxHighlighting.kt', 'w') as f:
    f.write(content)
