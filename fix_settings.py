import re

with open('./app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace DarkSurface with MaterialTheme.colorScheme.surfaceVariant or surface
content = content.replace('containerColor = DarkSurface', 'containerColor = MaterialTheme.colorScheme.surfaceVariant')

# Replace Color.White with MaterialTheme.colorScheme.onSurface
content = content.replace('color = Color.White', 'color = MaterialTheme.colorScheme.onSurface')

with open('./app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
