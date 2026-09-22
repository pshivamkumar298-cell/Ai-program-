package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String,
    val modelName: String = "gemini-3.5-flash",
    val language: String = "AUTO",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user" or "model"
    val content: String,
    val reasoningSummary: String? = null,
    val imageBase64: String? = null,
    val toolUsed: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "PREFERENCE", "PROJECT", "FACT"
    val title: String,
    val detail: String,
    val updatedAt: Long = System.currentTimeMillis()
)
