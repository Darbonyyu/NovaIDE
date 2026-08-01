import re
with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'private val repository: IdeRepository = IdeRepository(AppDatabase.getInstance(application))',
    'private val repository: IdeRepository = IdeRepository(AppDatabase.getInstance(application), application)'
)

with open('./app/src/main/java/com/example/ui/IdeViewModel.kt', 'w') as f:
    f.write(content)
