with open('./app/src/main/java/com/example/ui/MainScreen.kt', 'r') as f:
    content = f.read()

if 'rememberSaveableStateHolder' not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport androidx.compose.runtime.saveable.rememberSaveableStateHolder')
    content = content.replace('var currentTab by remember { mutableStateOf<NavTab>(NavTab.Chat) }', 'var currentTab by remember { mutableStateOf<NavTab>(NavTab.Chat) }\n    val saveableStateHolder = rememberSaveableStateHolder()')
    content = content.replace('Crossfade(targetState = currentTab, label = "TabSwitch") { tab ->\n                when (tab) {', 'Crossfade(targetState = currentTab, label = "TabSwitch") { tab ->\n                saveableStateHolder.SaveableStateProvider(tab) {\n                    when (tab) {')
    content = content.replace('}\n            }\n        }\n    }', '}\n                }\n            }\n        }\n    }')

with open('./app/src/main/java/com/example/ui/MainScreen.kt', 'w') as f:
    f.write(content)
