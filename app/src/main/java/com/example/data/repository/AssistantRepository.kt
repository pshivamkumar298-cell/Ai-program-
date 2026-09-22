package com.example.data.repository

import com.example.data.api.AssistantMode
import com.example.data.api.GeminiService
import com.example.data.api.GenerationResult
import com.example.data.db.AppDatabase
import com.example.data.db.ConversationEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AssistantRepository(private val database: AppDatabase) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val memoryDao = database.memoryDao()

    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun createConversation(
        title: String,
        mode: AssistantMode,
        modelName: String = "gemini-3.5-flash",
        language: String = "AUTO"
    ): Long {
        val entity = ConversationEntity(
            title = title,
            mode = mode.name,
            modelName = modelName,
            language = language
        )
        return conversationDao.insertConversation(entity)
    }

    suspend fun getConversation(id: Long): ConversationEntity? {
        return conversationDao.getConversationById(id)
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(id: Long) {
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversation(id)
    }

    suspend fun insertUserMessage(
        conversationId: Long,
        text: String,
        imageBase64: String? = null
    ): Long {
        val msg = MessageEntity(
            conversationId = conversationId,
            role = "user",
            content = text,
            imageBase64 = imageBase64
        )
        return messageDao.insertMessage(msg)
    }

    suspend fun askAssistant(
        conversationId: Long,
        prompt: String,
        mode: AssistantMode,
        modelName: String,
        language: String,
        imageBase64: String? = null
    ): GenerationResult {
        // Collect past turns from DB
        val pastMessages = messageDao.getMessagesForConversation(conversationId).firstOrNull() ?: emptyList()
        val history = pastMessages.map { it.role to it.content }

        // Collect memories
        val memories = memoryDao.getAllMemories().firstOrNull()?.map { "${it.title}: ${it.detail}" } ?: emptyList()

        val result = GeminiService.generateResponse(
            prompt = prompt,
            mode = mode,
            modelName = modelName,
            languagePreference = language,
            conversationHistory = history,
            imageBase64 = imageBase64,
            userMemories = memories
        )

        // Save assistant response
        messageDao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = "model",
                content = result.text,
                reasoningSummary = result.reasoningSummary,
                toolUsed = if (result.isDemo) "Local Sandbox" else "Gemini Live"
            )
        )

        return result
    }

    suspend fun saveMemory(category: String, title: String, detail: String): Long {
        return memoryDao.insertMemory(
            MemoryEntity(
                category = category,
                title = title,
                detail = detail
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }
}
