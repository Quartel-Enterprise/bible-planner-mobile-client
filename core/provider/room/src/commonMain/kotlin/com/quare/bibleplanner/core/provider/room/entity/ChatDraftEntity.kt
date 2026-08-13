package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * What the reader has typed into a chat thread's composer and not sent yet, so leaving the screen
 * or closing the app does not cost them the text. Reconciled with the server by the sync engine
 * (Last-Write-Wins by [updatedAtEpochMillis]).
 */
@Entity(tableName = "chat_drafts")
data class ChatDraftEntity(
    @PrimaryKey val threadKey: String,
    val content: String,
    val updatedAtEpochMillis: Long,
    val isPendingSync: Boolean,
)
