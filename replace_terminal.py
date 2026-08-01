import re

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'        // Integrated Bottom Terminal Drawer\n        AnimatedVisibility\(visible = isTerminalOpen\) \{[\s\S]*?\n                    \}\n                \}\n            \}\n        \}\n    \}',
    '''        // Integrated Bottom Terminal Drawer
        com.example.ui.components.workspace.TerminalView(
            isTerminalOpen = isTerminalOpen,
            terminalLogs = terminalLogs,
            terminalInput = terminalInput,
            onTerminalInputChange = { terminalInput = it },
            onCloseTerminal = { viewModel.toggleTerminal() },
            onRunCommand = { cmd -> 
                viewModel.runTerminalCommand(cmd)
                terminalInput = ""
            }
        )
    }''',
    content
)

with open('./app/src/main/java/com/example/ui/screens/ProjectsWorkspaceScreen.kt', 'w') as f:
    f.write(content)
