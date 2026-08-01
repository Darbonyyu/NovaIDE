import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

replacement = """                    IconButton(
                        onClick = { showCreateFileModal = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New File",
                            tint = AccentIndigo
                        )
                    }
                    IconButton(
                        onClick = { showCreateFolderModal = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            tint = AccentOrange
                        )
                    }
                    IconButton(
                        onClick = { showProjectSettingsModal = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Project Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }"""

content = re.sub(r'                    IconButton\(\n                        onClick = \{ showCreateFileModal = true \},[\s\S]*?tint = AccentIndigo\n                        \)\n                    \}', replacement, content)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
