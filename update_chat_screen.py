import re

with open('./app/src/main/java/com/example/ui/screens/ChatScreen.kt', 'r') as f:
    content = f.read()

replacement_call = """                MessageBubbleItem(
                    message = msg,
                    onCopyCode = { code -> viewModel.insertCodeToActiveTab(code) },
                    onInsertCode = { code -> viewModel.insertCodeToActiveTab(code); onNavigateToWorkspace() },
                    onReplaceCode = { code -> viewModel.replaceActiveTabCode(code); onNavigateToWorkspace() },
                    onSaveCode = { code, lang -> viewModel.saveCodeBlockAsFile(code, lang); onNavigateToWorkspace() },
                    onCompareCode = { code -> viewModel.openDiffCompare(code) },
                    onFollowUpSubmit = { block, prompt -> viewModel.followUpCodeBlock(block, prompt) },
                    onDelete = { viewModel.deleteMessage(msg.id) },
                    onEdit = { newText -> viewModel.editMessage(msg.id, newText) },
                    onRegenerate = { viewModel.regenerateMessage(msg.id) }
                )"""

content = re.sub(r'                MessageBubbleItem\([\s\S]*?onFollowUpSubmit = \{ block, prompt -> viewModel\.followUpCodeBlock\(block, prompt\) \}\n                \)', replacement_call, content)


replacement_def = """@Composable
fun MessageBubbleItem(
    message: ChatMessage,
    onCopyCode: (String) -> Unit,
    onInsertCode: (String) -> Unit,
    onReplaceCode: (String) -> Unit,
    onSaveCode: (String, String) -> Unit,
    onCompareCode: (String) -> Unit,
    onFollowUpSubmit: (CodeBlock, String) -> Unit,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    val isUser = message.sender == "user"
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(message.content) { mutableStateOf(message.content) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    val codeBlocks = remember(message.codeBlocksJson) {
        val list = mutableListOf<CodeBlock>()
        try {
            val jsonArr = JSONArray(message.codeBlocksJson)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(
                    CodeBlock(
                        id = obj.optString("id"),
                        code = obj.optString("code"),
                        language = obj.optString("language"),
                        explanation = obj.optString("explanation")
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    val timeString = timeFormat.format(java.util.Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isUser) AccentPurple else AccentIndigo,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "Вы" else "AI Assistant",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUser) AccentPurple else AccentIndigo
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeString,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) AccentPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                0.5.dp,
                if (isUser) AccentPurple.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {
                            isEditing = false
                            onEdit(editText)
                        }) {
                            Text("Сохранить", color = AccentIndigo)
                        }
                    }
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        // Actions row
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isEditing) {
                if (isUser) {
                    IconButton(onClick = { isEditing = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    IconButton(onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content))
                        android.widget.Toast.makeText(context, "Скопировано", android.widget.Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onRegenerate() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = { onDelete() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                }
            }
        }

        // Render Code Blocks if any
        codeBlocks.forEach { block ->
            CodeBlockCard("""

content = re.sub(r'@Composable\nfun MessageBubbleItem\([\s\S]*?CodeBlockCard\(', replacement_def, content)

with open('./app/src/main/java/com/example/ui/screens/ChatScreen.kt', 'w') as f:
    f.write(content)
