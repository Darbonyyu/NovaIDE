package com.example.ui.components.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange

@Composable
fun TerminalView(
    isTerminalOpen: Boolean,
    terminalLogs: List<String>,
    terminalInput: String,
    onTerminalInputChange: (String) -> Unit,
    onCloseTerminal: () -> Unit,
    onRunCommand: (String) -> Unit
) {
    AnimatedVisibility(visible = isTerminalOpen) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            color = Color(0xFF090A0F),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Интегрированный Терминал", style = MaterialTheme.typography.labelSmall, color = AccentOrange)
                    }
                    IconButton(onClick = onCloseTerminal, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Terminal", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(terminalLogs) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (log.startsWith("$ ")) AccentIndigo else Color.LightGray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$ ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AccentIndigo)
                    OutlinedTextField(
                        value = terminalInput,
                        onValueChange = onTerminalInputChange,
                        placeholder = { Text("команда (ls, cat, ai, help)...", fontSize = 11.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = { onRunCommand(terminalInput) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.SubdirectoryArrowLeft, contentDescription = "Run", tint = AccentOrange)
                    }
                }
            }
        }
    }
}
