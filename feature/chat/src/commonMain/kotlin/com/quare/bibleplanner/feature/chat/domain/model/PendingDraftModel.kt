package com.quare.bibleplanner.feature.chat.domain.model

/**
 * Text typed but not yet sent, kept with the thread it was typed into: saving it later must not
 * file it under whatever thread is open by then.
 */
data class PendingDraftModel(
    val threadKey: String,
    val content: String,
)
