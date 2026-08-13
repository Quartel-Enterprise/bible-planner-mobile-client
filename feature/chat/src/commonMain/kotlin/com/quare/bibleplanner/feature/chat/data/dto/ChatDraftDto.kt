package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `ai_chat_drafts` table: what the user has typed into one thread's composer and
 * not sent, keyed by (`user_id`, `thread_key`), reconciled by `updated_at` (Last-Write-Wins). An
 * empty [content] is a cleared draft — the sync engine never deletes rows.
 */
@Serializable
internal data class ChatDraftDto(
    @SerialName("user_id") val userId: String,
    @SerialName("thread_key") val threadKey: String,
    @SerialName("content") val content: String,
    @SerialName("updated_at") val updatedAt: String,
)
