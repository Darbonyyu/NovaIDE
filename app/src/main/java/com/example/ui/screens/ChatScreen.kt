package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessage
import com.example.data.models.CodeBlock
import com.example.ui.IdeViewModel
import com.example.ui.components.CodeBlockCard
import com.example.ui.theme.*
import org.json.JSONArray

@Composable
fun ChatScreen(
    viewModel: IdeViewModel,
    onNavigateToWorkspace: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val attachedFiles by viewModel.attachedFiles.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickActionChips = listOf(
        "🚀 Создать проект",
        "🐛 Найти баги",
        "⚡️ Оптимизировать",
        "🎨 Красивый UI",
        "📖 Оформить README",
        "🧪 Сгенерировать тесты"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header Status Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isGenerating) AccentOrange else AccentEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGenerating) "ИИ генерирует ответ..." else "Контекст проекта активен",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                MessageBubbleItem(
                    message = msg,
                    onCopyCode = { code -> viewModel.insertCodeToActiveTab(code) },
                    onInsertCode = { code -> viewModel.insertCodeToActiveTab(code); onNavigateToWorkspace() },
                    onReplaceCode = { code -> viewModel.replaceActiveTabCode(code); onNavigateToWorkspace() },
                    onSaveCode = { code, lang -> viewModel.saveCodeBlockAsFile(code, lang); onNavigateToWorkspace() },
                    onCompareCode = { code -> viewModel.openCodeDiff(code) },
                    onFollowUpSubmit = { block, prompt -> viewModel.followUpCodeBlock(block, prompt) },
                    onDelete = { viewModel.deleteMessage(msg.id) },
                    onEdit = { newText -> viewModel.editMessage(msg.id, newText) },
                    onRegenerate = { viewModel.regenerateMessage(msg.id) }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AccentIndigo,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI IDE анализирует код и файлы проекта...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentIndigo
                        )
                    }
                }
            }
        }

        // Attached Files Row (if any)
        if (attachedFiles.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                attachedFiles.forEach { fName ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentPurple.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentPurple)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = fName, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.removeAttachedFile(fName) }
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickActionChips.forEach { chip ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable {
                        viewModel.sendMessage(chip.substringAfter(" "))
                    }
                ) {
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Input Field Container
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.attachFileToChat("ScreenLayout_Mock.png")
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach File",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Спроси ИИ или набери задачу...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 110.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (isGenerating) {
                            viewModel.stopGeneration()
                        } else if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGenerating) MaterialTheme.colorScheme.error else if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isGenerating) "Stop" else "Send",
                        tint = if (isGenerating || inputText.isNotBlank()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).animateContentSize(),
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
            CodeBlockCard(
                codeBlock = block,
                onCopy = onCopyCode,
                onInsert = onInsertCode,
                onReplace = onReplaceCode,
                onSave = onSaveCode,
                onCompare = onCompareCode,
                onFollowUpSubmit = onFollowUpSubmit
            )
        }
    }
}
