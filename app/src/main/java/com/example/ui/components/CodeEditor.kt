package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.utils.buildAnnotatedCodeLine
import com.example.ui.utils.formatCode
import kotlinx.coroutines.delay

@Composable
fun CodeEditor(fontSize: Int = 13, 
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(code) {
        mutableStateOf(TextFieldValue(code))
    }

    // Debounce save
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text != code) {
            delay(500)
            onCodeChange(textFieldValue.text)
        }
    }

    val lines = textFieldValue.text.lines()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D14))
            .horizontalScroll(scrollState)
    ) {
        // Line numbers
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .fillMaxHeight(),
            horizontalAlignment = androidx.compose.ui.Alignment.End
        ) {
            for (i in lines.indices) {
                Text(
                    text = "${i + 1}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )
            }
        }

        // Code Area
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Check if user typed a bracket to auto-close
                var updatedValue = newValue
                if (newValue.text.length > textFieldValue.text.length) {
                    val addedChar = newValue.text.substring(textFieldValue.selection.start, newValue.selection.end).lastOrNull()
                    if (addedChar != null) {
                        updatedValue = handleAutoClose(updatedValue, addedChar)
                    }
                }
                textFieldValue = updatedValue
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(Color.White),
            visualTransformation = CodeVisualTransformation(MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )
    }
}

private fun handleAutoClose(value: TextFieldValue, addedChar: Char): TextFieldValue {
    val map = mapOf(
        '{' to '}',
        '(' to ')',
        '[' to ']',
        '"' to '"',
        '\'' to '\''
    )
    val closeChar = map[addedChar] ?: return value
    
    val text = value.text
    val selection = value.selection
    val newText = text.substring(0, selection.end) + closeChar + text.substring(selection.end)
    return TextFieldValue(newText, selection)
}

class CodeVisualTransformation(val onSurfaceColor: Color) : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val lines = text.text.lines()
        val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
            lines.forEachIndexed { index, line ->
                append(buildAnnotatedCodeLine(line, "kotlin", onSurfaceColor))
                if (index < lines.size - 1) append("\n")
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}
