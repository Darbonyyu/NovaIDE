import re

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'r') as f:
    content = f.read()

replacement = """        var showSearchReplace by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var replaceQuery by remember { mutableStateOf("") }

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
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { showSearchReplace = !showSearchReplace },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = if (showSearchReplace) AccentIndigo else Color.White, modifier = Modifier.size(16.dp))
            }
        }

        if (showSearchReplace) {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF333333)).padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                        modifier = Modifier.weight(1f).background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp)).padding(6.dp),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) androidx.compose.material3.Text("Найти...", color = Color.Gray, fontSize = 12.sp)
                            innerTextField()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                        modifier = Modifier.weight(1f).background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp)).padding(6.dp),
                        decorationBox = { innerTextField ->
                            if (replaceQuery.isEmpty()) androidx.compose.material3.Text("Заменить на...", color = Color.Gray, fontSize = 12.sp)
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                val newCode = textFieldValue.text.replace(searchQuery, replaceQuery)
                                if (newCode != textFieldValue.text) {
                                    textFieldValue = TextFieldValue(newCode)
                                    onCodeChange(newCode)
                                    // Let the LaunchedEffect handle undoStack logic via code change
                                    undoStack.add(newCode)
                                    redoStack.clear()
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        androidx.compose.material3.Text("Заменить все", fontSize = 11.sp)
                    }
                }
            }
        }
"""
content = re.sub(r'        // Toolbar\n        Row\([\s\S]*?modifier = Modifier\.size\(16\.dp\)\)\n            \}\n        \}', replacement, content)

with open('./app/src/main/java/com/example/ui/components/CodeEditor.kt', 'w') as f:
    f.write(content)
