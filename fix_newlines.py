import re

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

pattern = r'content = "\$fullResponse\n\n❌ Ошибка: \$\{e\.message\}",'
replacement = 'content = "$fullResponse\\n\\n❌ Ошибка: ${e.message}",'
new_content = re.sub(pattern, replacement, content)

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(new_content)
