package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AssistantMode
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PurpleRadiance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalTopBar(
    activeMode: AssistantMode,
    activeModel: String,
    languagePreference: String,
    onModeChange: (AssistantMode) -> Unit,
    onModelChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenMultimodal: () -> Unit,
    onOpenMemoryVault: () -> Unit,
    onNewChat: () -> Unit
) {
    var showModeMenu by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Drawer button + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("drawer_menu_button")
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Open Chat History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = ElectricIndigo.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Universal AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "General Purpose Intelligence",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Action tool icons & New Chat
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenCalculator,
                        modifier = Modifier.size(36.dp).testTag("open_calculator_button")
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Calculator Tool",
                            tint = CyanSpark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenMultimodal,
                        modifier = Modifier.size(36.dp).testTag("open_multimodal_button")
                    ) {
                        Icon(
                            Icons.Default.ImageSearch,
                            contentDescription = "Multimodal Tool",
                            tint = PurpleRadiance,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenMemoryVault,
                        modifier = Modifier.size(36.dp).testTag("open_memory_button")
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Conversation Memory",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier.size(36.dp).testTag("new_chat_button")
                    ) {
                        Icon(
                            Icons.Default.AddComment,
                            contentDescription = "Start New Chat",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Secondary row: Filter Chips (Mode, Model, Language)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Chip
                Box {
                    AssistChip(
                        onClick = { showModeMenu = true },
                        label = { Text(activeMode.title) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (activeMode) {
                                    AssistantMode.GENERAL -> Icons.Default.AutoAwesome
                                    AssistantMode.EDUCATION -> Icons.Default.School
                                    AssistantMode.PROGRAMMING -> Icons.Default.Code
                                    AssistantMode.MATH_DATA -> Icons.Default.Calculate
                                    AssistantMode.CREATIVE -> Icons.Default.Palette
                                    AssistantMode.BUSINESS -> Icons.AutoMirrored.Filled.TrendingUp
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ElectricIndigo
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { showModeMenu = false }
                    ) {
                        AssistantMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(mode.title, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            mode.tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (mode) {
                                            AssistantMode.GENERAL -> Icons.Default.AutoAwesome
                                            AssistantMode.EDUCATION -> Icons.Default.School
                                            AssistantMode.PROGRAMMING -> Icons.Default.Code
                                            AssistantMode.MATH_DATA -> Icons.Default.Calculate
                                            AssistantMode.CREATIVE -> Icons.Default.Palette
                                            AssistantMode.BUSINESS -> Icons.AutoMirrored.Filled.TrendingUp
                                        },
                                        contentDescription = null,
                                        tint = if (mode == activeMode) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    onModeChange(mode)
                                    showModeMenu = false
                                }
                            )
                        }
                    }
                }

                // Model Selector Chip
                Box {
                    AssistChip(
                        onClick = { showModelMenu = true },
                        label = {
                            Text(
                                if (activeModel.contains("pro")) "Pro 3.1" else "Flash 3.5",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CyanSpark
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = showModelMenu,
                        onDismissRequest = { showModelMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("gemini-3.5-flash", fontWeight = FontWeight.SemiBold)
                                    Text("Fastest, responsive general intelligence", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            onClick = {
                                onModelChange("gemini-3.5-flash")
                                showModelMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("gemini-3.1-pro-preview", fontWeight = FontWeight.SemiBold)
                                    Text("Complex reasoning, math & STEM", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            onClick = {
                                onModelChange("gemini-3.1-pro-preview")
                                showModelMenu = false
                            }
                        )
                    }
                }

                // Language Chip
                Box {
                    AssistChip(
                        onClick = { showLangMenu = true },
                        label = {
                            val label = when (languagePreference) {
                                "HINDI" -> "हिन्दी"
                                "HINGLISH" -> "Hinglish"
                                "ENGLISH" -> "English"
                                else -> "Auto"
                            }
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PurpleRadiance
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        listOf(
                            "AUTO" to "Auto-Detect (Matches input)",
                            "ENGLISH" to "English (Default)",
                            "HINDI" to "Hindi (हिन्दी)",
                            "HINGLISH" to "Hinglish (Hindi+English mix)"
                        ).forEach { (code, title) ->
                            DropdownMenuItem(
                                text = { Text(title) },
                                onClick = {
                                    onLanguageChange(code)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        }
    }
}
