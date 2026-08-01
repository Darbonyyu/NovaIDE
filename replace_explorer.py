import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'                        LazyColumn\(modifier = Modifier.fillMaxSize\(\)\) \{[\s\S]*?                                \}\n                            \}\n                        \}',
    '''                        com.example.ui.components.workspace.FileExplorer(
                            currentFiles = currentFiles,
                            activeTabId = activeTabId,
                            onOpenFile = { viewModel.openFileTab(it) },
                            onDeleteFile = { viewModel.deleteFile(it) }
                        )''',
    content
)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
