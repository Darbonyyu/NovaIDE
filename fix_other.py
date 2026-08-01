import os
import re

for root, _, files in os.walk('./app/src/main/java/com/example/ui'):
    for file in files:
        if not file.endswith('.kt'): continue
        path = os.path.join(root, file)
        with open(path, 'r') as f:
            content = f.read()
        
        # We don't want to change Color.kt and Theme.kt
        if file in ['Color.kt', 'Theme.kt']: continue
        
        orig = content
        
        content = content.replace('containerColor = DarkSurface', 'containerColor = MaterialTheme.colorScheme.surfaceVariant')
        content = content.replace('DarkSurfaceVariant.copy', 'MaterialTheme.colorScheme.surfaceVariant.copy')
        content = content.replace('background(DarkSurfaceVariant)', 'background(MaterialTheme.colorScheme.surfaceVariant)')
        content = content.replace('containerColor = DarkSurface', 'containerColor = MaterialTheme.colorScheme.surface')
        content = content.replace('unfocusedContainerColor = DarkSurface', 'unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant')
        content = content.replace('focusedContainerColor = DarkSurface', 'focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant')
        content = content.replace('color = Color.White', 'color = MaterialTheme.colorScheme.onSurface')
        content = content.replace('Color(0xFF090A0F)', 'MaterialTheme.colorScheme.background')
        
        if orig != content:
            with open(path, 'w') as f:
                f.write(content)
