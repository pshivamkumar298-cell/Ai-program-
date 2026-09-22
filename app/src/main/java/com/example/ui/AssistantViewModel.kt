package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AssistantMode
import com.example.data.api.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.db.ConversationEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.MessageEntity
import com.example.data.repository.AssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantUiState(
    val currentConversationId: Long? = null,
    val currentConversationTitle: String = "Universal AI Assistant",
    val activeMode: AssistantMode = AssistantMode.GENERAL,
    val activeModel: String = "gemini-3.5-flash",
    val languagePreference: String = "AUTO", // AUTO, ENGLISH, HINDI, HINGLISH
    val isLoading: Boolean = false,
    val conversations: List<ConversationEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val memories: List<MemoryEntity> = emptyList(),
    val attachedImageBase64: String? = null,
    val attachedImageName: String? = null,
    val isApiKeyConfigured: Boolean = false,
    val errorMessage: String? = null
)

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AssistantRepository(database)

    private val _uiState = MutableStateFlow(
        AssistantUiState(isApiKeyConfigured = GeminiService.isApiKeyConfigured())
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        // Observe conversations
        viewModelScope.launch {
            repository.allConversations.collectLatest { convs ->
                _uiState.update { it.copy(conversations = convs) }
                if (convs.isNotEmpty() && _uiState.value.currentConversationId == null) {
                    selectConversation(convs.first().id)
                } else if (convs.isEmpty()) {
                    startFreshConversation(_uiState.value.activeMode)
                }
            }
        }

        // Observe memories
        viewModelScope.launch {
            repository.allMemories.collectLatest { mems ->
                _uiState.update { it.copy(memories = mems) }
            }
        }
    }

    fun startFreshConversation(mode: AssistantMode = _uiState.value.activeMode) {
        viewModelScope.launch {
            val title = when (mode) {
                AssistantMode.GENERAL -> "Universal Reasoning"
                AssistantMode.EDUCATION -> "Academic Inquiry"
                AssistantMode.PROGRAMMING -> "Architecture & Code"
                AssistantMode.MATH_DATA -> "Mathematical Problem"
                AssistantMode.CREATIVE -> "Creative Exploration"
                AssistantMode.BUSINESS -> "Strategic Analysis"
            }
            val newId = repository.createConversation(
                title = title,
                mode = mode,
                modelName = _uiState.value.activeModel,
                language = _uiState.value.languagePreference
            )
            selectConversation(newId)
        }
    }

    fun selectConversation(id: Long) {
        _uiState.update { it.copy(currentConversationId = id) }
        viewModelScope.launch {
            val conv = repository.getConversation(id)
            if (conv != null) {
                val mode = try {
                    AssistantMode.valueOf(conv.mode)
                } catch (e: Exception) {
                    AssistantMode.GENERAL
                }
                _uiState.update {
                    it.copy(
                        currentConversationTitle = conv.title,
                        activeMode = mode,
                        activeModel = conv.modelName,
                        languagePreference = conv.language
                    )
                }
            }
            repository.getMessages(id).collectLatest { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun setMode(mode: AssistantMode) {
        _uiState.update { it.copy(activeMode = mode) }
        val currentId = _uiState.value.currentConversationId
        if (currentId != null) {
            viewModelScope.launch {
                val conv = repository.getConversation(currentId)
                if (conv != null) {
                    repository.updateConversation(conv.copy(mode = mode.name))
                }
            }
        }
    }

    fun setModel(model: String) {
        _uiState.update { it.copy(activeModel = model) }
        val currentId = _uiState.value.currentConversationId
        if (currentId != null) {
            viewModelScope.launch {
                val conv = repository.getConversation(currentId)
                if (conv != null) {
                    repository.updateConversation(conv.copy(modelName = model))
                }
            }
        }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(languagePreference = lang) }
        val currentId = _uiState.value.currentConversationId
        if (currentId != null) {
            viewModelScope.launch {
                val conv = repository.getConversation(currentId)
                if (conv != null) {
                    repository.updateConversation(conv.copy(language = lang))
                }
            }
        }
    }

    fun attachImage(base64: String, name: String) {
        _uiState.update {
            it.copy(
                attachedImageBase64 = base64,
                attachedImageName = name
            )
        }
    }

    fun clearAttachedImage() {
        _uiState.update {
            it.copy(
                attachedImageBase64 = null,
                attachedImageName = null
            )
        }
    }

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty() && _uiState.value.attachedImageBase64 == null) return

        val conversationId = _uiState.value.currentConversationId ?: return
        val currentMode = _uiState.value.activeMode
        val currentModel = _uiState.value.activeModel
        val currentLang = _uiState.value.languagePreference
        val imageBase64 = _uiState.value.attachedImageBase64

        // Clear input attachment
        clearAttachedImage()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Insert user message in DB
            repository.insertUserMessage(
                conversationId = conversationId,
                text = trimmed.ifEmpty { "Analyze this image" },
                imageBase64 = imageBase64
            )

            // Update conversation title if it's the first message
            val conv = repository.getConversation(conversationId)
            if (conv != null && (conv.title.startsWith("Universal") || conv.title.startsWith("Academic") || conv.title.startsWith("Architecture") || conv.title.startsWith("Mathematical") || conv.title.startsWith("Creative") || conv.title.startsWith("Strategic"))) {
                val smartTitle = if (trimmed.length > 28) trimmed.take(25) + "..." else trimmed
                if (smartTitle.isNotBlank()) {
                    repository.updateConversation(conv.copy(title = smartTitle))
                    _uiState.update { it.copy(currentConversationTitle = smartTitle) }
                }
            }

            // Call Gemini
            val result = repository.askAssistant(
                conversationId = conversationId,
                prompt = trimmed.ifEmpty { "Analyze this image and describe key insights, formulas, and structural observations." },
                mode = currentMode,
                modelName = currentModel,
                language = currentLang,
                imageBase64 = imageBase64
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.errorMessage
                )
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_uiState.value.currentConversationId == id) {
                val remaining = _uiState.value.conversations.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    startFreshConversation()
                }
            }
        }
    }

    fun saveMemory(category: String, title: String, detail: String) {
        viewModelScope.launch {
            repository.saveMemory(category, title, detail)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
