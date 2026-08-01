import re

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'r') as f:
    content = f.read()

replacement = """fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }
    
    var textFieldValue by remember(code) {
        if (undoStack.isEmpty() || undoStack.last() != code) {
            undoStack.add(code)
            redoStack.clear()
        }
        mutableStateOf(TextFieldValue(code))
    }

    // Debounce save
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text != code) {
            onCodeChange(textFieldValue.text)
        }
    }

    Column(modifier = modifier.background(Color(0xFF1E1E1E))) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (undoStack.size > 1) {
                        redoStack.add(undoStack.removeLast())
                        val previous = undoStack.last()
                        textFieldValue = TextFieldValue(previous)
                        onCodeChange(previous)
                    }
                },
                enabled = undoStack.size > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.size > 1) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = {
                    if (redoStack.isNotEmpty()) {
                        val next = redoStack.removeLast()
                        undoStack.add(next)
                        textFieldValue = TextFieldValue(next)
                        onCodeChange(next)
                    }
                },
                enabled = redoStack.isNotEmpty(),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Redo, contentDescription = "Redo", tint = if (redoStack.isNotEmpty()) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
"""
content = re.sub(r'fun CodeEditor\([\s\S]*?Column\(modifier = modifier\.background\(Color\(0xFF1E1E1E\)\)\) \{', replacement, content)

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'w') as f:
    f.write(content)
