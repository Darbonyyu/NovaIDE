package com.example.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType

fun buildAnnotatedCodeLine(line: String, lang: String): AnnotatedString = buildAnnotatedString {
    val trimmed = line.trimStart()
    if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("/*") || trimmed.startsWith("* ")) {
        withStyle(style = SpanStyle(color = SyntaxComment)) {
            append(line)
        }
        return@buildAnnotatedString
    }

    val words = line.split(Regex("(?<=\\s)|(?=\\s)|(?<=[(),.<>:;=+\\-*/{}\\[\\]])|(?=[(),.<>:;=+\\-*/{}\\[\\]])"))

    val keywords = setOf(
        "fun", "val", "var", "class", "object", "sealed", "interface", "import", "package", "return",
        "if", "else", "when", "for", "while", "suspend", "private", "public", "protected", "override",
        "const", "type", "export", "let", "def", "async", "await", "from", "data", "true", "false", "null",
        "Modifier", "Composable"
    )

    words.forEach { word ->
        when {
            keywords.contains(word) -> {
                withStyle(style = SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold)) {
                    append(word)
                }
            }
            word.startsWith("\"") || word.endsWith("\"") || word.startsWith("'") || word.endsWith("'") -> {
                withStyle(style = SpanStyle(color = SyntaxString)) {
                    append(word)
                }
            }
            word.toIntOrNull() != null || word.toDoubleOrNull() != null -> {
                withStyle(style = SpanStyle(color = SyntaxNumber)) {
                    append(word)
                }
            }
            word.firstOrNull()?.isUpperCase() == true -> {
                withStyle(style = SpanStyle(color = SyntaxType)) {
                    append(word)
                }
            }
            else -> {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append(word)
                }
            }
        }
    }
}

fun formatCode(code: String): String {
    // Basic formatting logic
    var indentLevel = 0
    val indentString = "    "
    val lines = code.lines()
    val formatted = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            formatted.append("\n")
            continue
        }

        if (trimmed.startsWith("}") || trimmed.startsWith("]")) {
            indentLevel = maxOf(0, indentLevel - 1)
        }

        formatted.append(indentString.repeat(indentLevel)).append(trimmed).append("\n")

        if (trimmed.endsWith("{") || trimmed.endsWith("[")) {
            indentLevel++
        }
    }
    return formatted.toString().trimEnd()
}
