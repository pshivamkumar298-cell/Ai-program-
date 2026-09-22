package com.example.ui.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PurpleRadiance
import java.text.DecimalFormat
import kotlin.math.*

@Composable
fun CalculatorDialog(
    onDismiss: () -> Unit,
    onSendToChat: (String) -> Unit
) {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    var showHistory by remember { mutableStateOf(false) }

    fun appendInput(char: String) {
        if (display == "0" && char !in listOf("+", "-", "×", "÷", "%", "^")) {
            display = char
        } else {
            display += char
        }
    }

    fun clearAll() {
        display = "0"
        expression = ""
    }

    fun backspace() {
        display = if (display.length > 1) display.dropLast(1) else "0"
    }

    fun evaluateExpression() {
        try {
            val sanitized = display
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())

            val result = simpleEvaluate(sanitized)
            val formatter = DecimalFormat("#.########")
            val formatted = formatter.format(result)
            val calculationRecord = "$display = $formatted"
            history = listOf(calculationRecord) + history.take(9)
            expression = display
            display = formatted
        } catch (e: Exception) {
            display = "Error"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("calculator_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculator",
                        tint = CyanSpark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Precision Math Tool",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Calculation screen
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (expression.isNotEmpty()) {
                            Text(
                                text = expression,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = display,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showHistory = !showHistory }) {
                        Text(if (showHistory) "Show Keypad" else "Recent Calculations (${history.size})")
                    }
                    if (display != "0" && display != "Error") {
                        FilledTonalButton(
                            onClick = {
                                onSendToChat("Please explain and verify this calculation: ${if (expression.isNotEmpty()) "$expression = $display" else display}")
                                onDismiss()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use in Chat", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (showHistory) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        if (history.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No calculation history yet", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            items(history) { record ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            val parts = record.split(" = ")
                                            if (parts.size == 2) {
                                                display = parts[1]
                                                expression = parts[0]
                                                showHistory = false
                                            }
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = record,
                                        modifier = Modifier.padding(10.dp),
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Calculator buttons grid
                    val buttonRows = listOf(
                        listOf("C", "(", ")", "÷"),
                        listOf("7", "8", "9", "×"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("0", ".", "⌫", "=")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        buttonRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { btn ->
                                    val isOp = btn in listOf("÷", "×", "-", "+", "=")
                                    val isAction = btn in listOf("C", "⌫", "(", ")")
                                    val btnColor = when {
                                        btn == "=" -> ElectricIndigo
                                        isOp -> CyanSpark.copy(alpha = 0.85f)
                                        isAction -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    val txtColor = when {
                                        btn == "=" -> Color.White
                                        isOp -> Color.Black
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                when (btn) {
                                                    "C" -> clearAll()
                                                    "⌫" -> backspace()
                                                    "=" -> evaluateExpression()
                                                    else -> appendInput(btn)
                                                }
                                            },
                                        color = btnColor,
                                        tonalElevation = 2.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (btn == "⌫") {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.Backspace,
                                                    contentDescription = "Backspace",
                                                    modifier = Modifier.size(18.dp),
                                                    tint = txtColor
                                                )
                                            } else {
                                                Text(
                                                    text = btn,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = txtColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Simple arithmetic & basic expression parser
private fun simpleEvaluate(expr: String): Double {
    val clean = expr.replace(" ", "")
    var pos = -1
    var ch = -1

    fun nextChar() {
        ch = if (++pos < clean.length) clean[pos].code else -1
    }

    fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor()
        if (eat('-'.code)) return -parseFactor()

        var x: Double
        val startPos = pos
        if (eat('('.code)) {
            // function parseExpression
            x = run {
                // inner parseExpression
                var innerX = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> innerX *= parseFactor()
                        eat('/'.code) -> innerX /= parseFactor()
                        eat('%'.code) -> innerX %= parseFactor()
                        else -> break
                    }
                }
                while (true) {
                    when {
                        eat('+'.code) -> innerX += parseFactor()
                        eat('-'.code) -> innerX -= parseFactor()
                        else -> break
                    }
                }
                innerX
            }
            eat(')'.code)
        } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
            while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
            x = clean.substring(startPos, pos).toDouble()
        } else {
            return 0.0
        }
        return x
    }

    // Direct token evaluation
    nextChar()
    val tokens = mutableListOf<String>()
    var currentNum = ""
    for (i in clean.indices) {
        val c = clean[i]
        if (c in listOf('+', '-', '*', '/', '%')) {
            if (currentNum.isNotEmpty()) {
                tokens.add(currentNum)
                currentNum = ""
            }
            tokens.add(c.toString())
        } else {
            currentNum += c
        }
    }
    if (currentNum.isNotEmpty()) tokens.add(currentNum)

    if (tokens.isEmpty()) return 0.0
    if (tokens.size == 1) return tokens[0].toDoubleOrNull() ?: 0.0

    // Evaluate * and / and %
    val postMult = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val tok = tokens[i]
        if (tok == "*" || tok == "/" || tok == "%") {
            val left = postMult.removeAt(postMult.size - 1).toDouble()
            val right = tokens[i + 1].toDouble()
            val res = when (tok) {
                "*" -> left * right
                "/" -> if (right != 0.0) left / right else Double.NaN
                else -> left % right
            }
            postMult.add(res.toString())
            i += 2
        } else {
            postMult.add(tok)
            i++
        }
    }

    // Evaluate + and -
    var finalResult = postMult.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    var j = 1
    while (j < postMult.size) {
        val op = postMult[j]
        val right = postMult.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
        finalResult = if (op == "+") finalResult + right else finalResult - right
        j += 2
    }
    return finalResult
}
