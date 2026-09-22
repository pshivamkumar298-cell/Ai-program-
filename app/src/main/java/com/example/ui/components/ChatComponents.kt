package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AssistantMode
import com.example.data.db.MessageEntity
import com.example.ui.theme.*

@Composable
fun MessageBubble(
    message: MessageEntity,
    onPromptSuggestionClick: (String) -> Unit = {}
) {
    val isUser = message.role.equals("user", ignoreCase = true)
    val context = LocalContext.current
    var showReasoning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            Surface(
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                color = ElectricIndigo,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .testTag("user_message_bubble")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Show attached image if present
                    if (!message.imageBase64.isNullOrEmpty()) {
                        val bitmap = remember(message.imageBase64) {
                            try {
                                val bytes = Base64.decode(message.imageBase64, Base64.NO_WRAP)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Attached visual content",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        } else {
            // Assistant Card
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .testTag("assistant_message_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header with mode & reasoning pipeline indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = CyanSpark.copy(alpha = 0.15f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = CyanSpark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Universal AI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanSpark
                            )
                            if (message.toolUsed != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${message.toolUsed}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Copy button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AI Response", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 6-step reasoning banner
                    if (!message.reasoningSummary.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showReasoning = !showReasoning }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LinearScale,
                                    contentDescription = null,
                                    tint = PurpleRadiance,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.reasoningSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PurpleRadiance,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (showReasoning) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = showReasoning) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Reasoning Execution Matrix:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "1. UNDERSTAND: Semantic intent parsed and bounded.\n2. THINK: Verified constraints and logical path.\n3. CREATE: Synthesized structured solution formulation.\n4. SOLVE: Evaluated derivation with factual checks.\n5. DECIDE: Minimized ambiguity and verified risks.\n6. ACT: Emitted actionable response.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Message Body with rich formatting parser
                    FormattedMarkdownContent(message.content)
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownContent(content: String) {
    val context = LocalContext.current
    val sections = remember(content) { parseMarkdownBlocks(content) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sections.forEach { block ->
            when (block) {
                is ContentBlock.Code -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = block.language.ifEmpty { "code" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanSpark,
                                    fontFamily = FontFamily.Monospace
                                )
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Code", block.code)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Text(
                                text = block.code,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(0xFFF1F5F9)
                                )
                            )
                        }
                    }
                }
                is ContentBlock.Text -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

sealed class ContentBlock {
    data class Text(val text: String) : ContentBlock()
    data class Code(val language: String, val code: String) : ContentBlock()
}

private fun parseMarkdownBlocks(text: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val codeFenceRegex = "```([a-zA-Z0-9_-]*)\n([\\s\\S]*?)```".toRegex()
    var lastIndex = 0

    codeFenceRegex.findAll(text).forEach { matchResult ->
        val range = matchResult.range
        if (range.first > lastIndex) {
            val normalText = text.substring(lastIndex, range.first).trim()
            if (normalText.isNotEmpty()) {
                blocks.add(ContentBlock.Text(normalText))
            }
        }
        val lang = matchResult.groupValues[1].trim()
        val code = matchResult.groupValues[2].trimEnd()
        blocks.add(ContentBlock.Code(lang, code))
        lastIndex = range.last + 1
    }

    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            blocks.add(ContentBlock.Text(remaining))
        }
    }

    if (blocks.isEmpty() && text.isNotEmpty()) {
        blocks.add(ContentBlock.Text(text))
    }

    return blocks
}

@Composable
fun QuickPromptRow(
    mode: AssistantMode,
    onSelectPrompt: (String) -> Unit
) {
    val suggestions = remember(mode) {
        when (mode) {
            AssistantMode.GENERAL -> listOf(
                "Deconstruct complex problem",
                "Compare 3 solution paths",
                "Explain reasoning conclusion",
                "Detect ambiguity in premise"
            )
            AssistantMode.EDUCATION -> listOf(
                "Explain intuitive concept",
                "Solve step-by-step formula",
                "Create a 7-day revision plan",
                "Generate 3 practice questions"
            )
            AssistantMode.PROGRAMMING -> listOf(
                "Review system architecture",
                "Find edge-case bugs & leaks",
                "Write clean Kotlin Compose",
                "Design test suite & mocks"
            )
            AssistantMode.MATH_DATA -> listOf(
                "Calculate loan EMI & interest",
                "Derive calculus equation",
                "Evaluate statistical variance",
                "Unit conversion & precision"
            )
            AssistantMode.CREATIVE -> listOf(
                "Compelling opening narrative",
                "5 viral tech hook scripts",
                "Brainstorm 10 product names",
                "Write poem on neural networks"
            )
            AssistantMode.BUSINESS -> listOf(
                "Identify constraints & risks",
                "Evaluate trade-off matrix",
                "30-day execution roadmap",
                "Unit economics breakdown"
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { prompt ->
            SuggestionChip(
                onClick = { onSelectPrompt(prompt) },
                label = {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}
