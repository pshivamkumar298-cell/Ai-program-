package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AssistantMode
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PurpleRadiance

@Composable
fun ChatInputBar(
    activeMode: AssistantMode,
    attachedImageBase64: String?,
    attachedImageName: String?,
    isLoading: Boolean,
    onClearImage: () -> Unit,
    onOpenMultimodal: () -> Unit,
    onOpenCalculator: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    val placeholderText = when (activeMode) {
        AssistantMode.GENERAL -> "Ask anything in English, हिन्दी, or Hinglish..."
        AssistantMode.EDUCATION -> "Ask about a concept, formula, or problem..."
        AssistantMode.PROGRAMMING -> "Describe architecture, paste code, or ask for tests..."
        AssistantMode.MATH_DATA -> "Enter math equation, stats, or financial problem..."
        AssistantMode.CREATIVE -> "Enter creative story premise, script, or hook..."
        AssistantMode.BUSINESS -> "Describe business case, constraints, or dilemma..."
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Attached image preview banner
            AnimatedVisibility(visible = attachedImageBase64 != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanSpark.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bitmap = remember(attachedImageBase64) {
                            try {
                                if (attachedImageBase64 != null) {
                                    val bytes = Base64.decode(attachedImageBase64, Base64.NO_WRAP)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Attached thumbnail",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = CyanSpark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attachedImageName ?: "Image attached",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                            Text(
                                text = "Ready for multimodal analysis",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = CyanSpark
                            )
                        }

                        IconButton(
                            onClick = onClearImage,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove attached image",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multimodal attachment button
                IconButton(
                    onClick = onOpenMultimodal,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("attach_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach image for multimodal analysis",
                        tint = if (attachedImageBase64 != null) CyanSpark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Precision Math shortcut button
                IconButton(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("calculator_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Precision calculator",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() || attachedImageBase64 != null) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send button
                FloatingActionButton(
                    onClick = {
                        if ((inputText.isNotBlank() || attachedImageBase64 != null) && !isLoading) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("send_message_button"),
                    shape = CircleShape,
                    containerColor = ElectricIndigo,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
