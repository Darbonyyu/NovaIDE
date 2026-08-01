import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'                // Editor Content / Live Preview Switch\n                if \(isLivePreviewOpen && activeFile != null\) \{[\s\S]*?                                color = MaterialTheme.colorScheme.onSurfaceVariant\n                            \)\n                        \}\n                    \}\n                \}',
    '''                // Editor Content / Live Preview Switch
                com.example.ui.components.workspace.WorkspaceEditor(
                    activeFile = activeFile,
                    isLivePreviewOpen = isLivePreviewOpen,
                    editorTextState = editorTextState,
                    onCodeChange = {
                        editorTextState = it
                        viewModel.updateActiveFileContent(it)
                    }
                )''',
    content
)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
