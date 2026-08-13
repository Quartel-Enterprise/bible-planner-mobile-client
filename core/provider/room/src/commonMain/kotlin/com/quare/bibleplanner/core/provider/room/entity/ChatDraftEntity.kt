package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "chat_drafts")
data class ChatDraftEntity(
    @PrimaryKey val threadKey: String,
    val content: String,
    val updatedAtEpochMillis: Long,
    val isPendingSync: Boolean,
)
