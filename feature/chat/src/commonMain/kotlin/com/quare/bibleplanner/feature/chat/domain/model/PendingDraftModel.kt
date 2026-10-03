package com.quare.bibleplanner.feature.chat.domain.model

// Why: the draft keeps the thread it was typed into, so saving it later never files it
// under whatever thread is open by then.
data class PendingDraftModel(
    val threadKey: String,
    val content: String,
)
