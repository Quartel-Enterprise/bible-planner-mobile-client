package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * A persisted chat message. Only answers the server has confirmed land here — the one being
 * streamed lives in memory until it completes, so a half-written answer is never cached as if it
 * were the real one.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val isFromUser: Boolean,
    val content: String,
    val createdAtEpochMillis: Long,
)
