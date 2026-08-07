package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Read-only mirror of a server conversation, so the history list and its threads can be read
 * offline. The server owns every field; nothing here is ever pushed back.
 */
@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val preview: String?,
    val contextLabel: String?,
    val updatedAtEpochMillis: Long,
)
