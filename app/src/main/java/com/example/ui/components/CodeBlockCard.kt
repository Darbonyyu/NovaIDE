package com.example.ui.components

import com.example.ui.utils.buildAnnotatedCodeLine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.ui.utils.buildAnnotatedCodeLine
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CodeBlock
import com.example.ui.theme.*

@Composable
fun CodeBlockCard(
    codeBlock: CodeBlock,
    onCopy: (String) -> Unit,
    onInsert: (String) -> Unit,
    onReplace: (String) -> Unit,
    onSave: (String, String) -> Unit,
    onCompare: (String) -> Unit,
    onFollowUpSubmit: (CodeBlock, String) -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    var followUpInput by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }

    val presetChips = listOf(
        "добавь авторизацию",
        "исправь ошибку",
        "сделай красивее",
        "оптимизируй",
        "переведи на Kotlin",
        "раздели на файлы"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Color(0x2BFFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentIndigo)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = codeBlock.language.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentIndigo
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", codeBlock.code))
                            isCopied = true
                            onCopy(codeBlock.code)
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (isCopied) AccentEmerald else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onInsert(codeBlock.code) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = "Insert",
                            tint = AccentIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onReplace(codeBlock.code) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FindReplace,
                            contentDescription = "Replace",
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSave(codeBlock.code, codeBlock.language) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = "Save",
                            tint = AccentOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCompare(codeBlock.code) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Compare Diff",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code Content Body with Line Numbers & Syntax Color
            val lines = codeBlock.code.split("\n")
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0D14))
                    .padding(vertical = 12.dp)
                    .horizontalScroll(scrollState)
            ) {
                // Line Numbers Column
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.indices.forEach { index ->
                        Text(
                            text = "${index + 1}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                // Code Lines Column with Syntax Colorizing
                Column(modifier = Modifier.padding(end = 16.dp)) {
                    lines.forEach { line ->
                        Text(
                            text = buildAnnotatedCodeLine(line, codeBlock.language),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Action Prompt Row: "Что изменить в этом коде?"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Что изменить в этом коде?",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentIndigo,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Preset Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetChips.forEach { chipText ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, Color(0x33FFFFFF)),
                            modifier = Modifier.clickable {
                                onFollowUpSubmit(codeBlock, chipText)
                            }
                        ) {
                            Text(
                                text = "+ $chipText",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Follow up text input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = followUpInput,
                        onValueChange = { followUpInput = it },
                        placeholder = { Text("Введите пожелания к коду...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = AccentIndigo,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (followUpInput.isNotBlank()) {
                                onFollowUpSubmit(codeBlock, followUpInput)
                                followUpInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentIndigo)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Submit modification",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

