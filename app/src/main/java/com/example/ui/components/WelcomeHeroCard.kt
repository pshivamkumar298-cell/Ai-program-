package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AssistantMode
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PurpleRadiance

@Composable
fun WelcomeHeroCard(
    activeMode: AssistantMode,
    onSelectMode: (AssistantMode) -> Unit,
    onSendPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Radiant Emblem
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(ElectricIndigo, PurpleRadiance, Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Universal AI",
                        tint = ElectricIndigo,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "UNIVERSAL AI ASSISTANT",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Reasoning • Learning • Coding • Math • Creativity • Multimodal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Reasoning badge pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UNDERSTAND → THINK → CREATE → SOLVE → DECIDE → ACT",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = CyanSpark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mode cards grid
        Text(
            text = "Select Specialized Intelligence Mode:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        val modes = listOf(
            AssistantMode.GENERAL to ("Universal Reasoner" to Icons.Default.AutoAwesome),
            AssistantMode.EDUCATION to ("Academic Tutor" to Icons.Default.School),
            AssistantMode.PROGRAMMING to ("Coding & Tech" to Icons.Default.Code),
            AssistantMode.MATH_DATA to ("Math & Precision" to Icons.Default.Calculate),
            AssistantMode.CREATIVE to ("Creative Studio" to Icons.Default.Palette),
            AssistantMode.BUSINESS to ("Business Strategy" to Icons.AutoMirrored.Filled.TrendingUp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            modes.chunked(2).forEach { rowModes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowModes.forEach { (mode, info) ->
                        val isCurrent = mode == activeMode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (isCurrent) ElectricIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectMode(mode)
                                    onSendPrompt(mode.defaultPrompt)
                                },
                            color = if (isCurrent) ElectricIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = info.second,
                                    contentDescription = null,
                                    tint = if (isCurrent) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = info.first,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = mode.tag,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
