package com.example.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.api.GeminiService
import com.example.ui.components.*
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricIndigo
import com.example.ui.tools.CalculatorDialog
import com.example.ui.tools.MemoryVaultDialog
import com.example.ui.tools.MultimodalSheet
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: AssistantViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Dialog & Sheet States
    var showCalculator by remember { mutableStateOf(false) }
    var showMultimodalSheet by remember { mutableStateOf(false) }
    var showMemoryVault by remember { mutableStateOf(false) }

    // System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val base64 = GeminiService.bitmapToBase64(bitmap)
                    viewModel.attachImage(base64, "Device Photo")
                }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = uiState.conversations,
                currentConversationId = uiState.currentConversationId,
                onSelectConversation = { id -> viewModel.selectConversation(id) },
                onDeleteConversation = { id -> viewModel.deleteConversation(id) },
                onNewChat = { viewModel.startFreshConversation() },
                onOpenMemoryVault = { showMemoryVault = true },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                UniversalTopBar(
                    activeMode = uiState.activeMode,
                    activeModel = uiState.activeModel,
                    languagePreference = uiState.languagePreference,
                    onModeChange = { mode -> viewModel.setMode(mode) },
                    onModelChange = { model -> viewModel.setModel(model) },
                    onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onOpenCalculator = { showCalculator = true },
                    onOpenMultimodal = { showMultimodalSheet = true },
                    onOpenMemoryVault = { showMemoryVault = true },
                    onNewChat = { viewModel.startFreshConversation() }
                )
            },
            bottomBar = {
                Column {
                    QuickPromptRow(
                        mode = uiState.activeMode,
                        onSelectPrompt = { prompt -> viewModel.sendMessage(prompt) }
                    )
                    ChatInputBar(
                        activeMode = uiState.activeMode,
                        attachedImageBase64 = uiState.attachedImageBase64,
                        attachedImageName = uiState.attachedImageName,
                        isLoading = uiState.isLoading,
                        onClearImage = { viewModel.clearAttachedImage() },
                        onOpenMultimodal = { showMultimodalSheet = true },
                        onOpenCalculator = { showCalculator = true },
                        onSendMessage = { text -> viewModel.sendMessage(text) }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_screen")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.messages.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            WelcomeHeroCard(
                                activeMode = uiState.activeMode,
                                onSelectMode = { mode -> viewModel.setMode(mode) },
                                onSendPrompt = { prompt -> viewModel.sendMessage(prompt) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                onPromptSuggestionClick = { prompt -> viewModel.sendMessage(prompt) }
                            )
                        }

                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = ElectricIndigo,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Reasoning with ${uiState.activeModel}...",
                                            style = MaterialTheme.typography.bodySmall,
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

    // Calculator Tool Dialog
    if (showCalculator) {
        CalculatorDialog(
            onDismiss = { showCalculator = false },
            onSendToChat = { prompt ->
                viewModel.sendMessage(prompt)
                showCalculator = false
            }
        )
    }

    // Memory Vault Dialog
    if (showMemoryVault) {
        MemoryVaultDialog(
            memories = uiState.memories,
            onDismiss = { showMemoryVault = false },
            onSaveMemory = { cat, title, detail ->
                viewModel.saveMemory(cat, title, detail)
            },
            onDeleteMemory = { id ->
                viewModel.deleteMemory(id)
            }
        )
    }

    // Multimodal Sheet
    if (showMultimodalSheet) {
        MultimodalSheet(
            onDismiss = { showMultimodalSheet = false },
            onSelectImage = { base64, prompt, name ->
                viewModel.attachImage(base64, name)
                viewModel.sendMessage(prompt)
                showMultimodalSheet = false
            },
            onLaunchPhotoPicker = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}
