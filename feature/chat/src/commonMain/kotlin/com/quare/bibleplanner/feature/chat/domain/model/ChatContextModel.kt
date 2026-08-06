package com.quare.bibleplanner.feature.chat.domain.model

import com.quare.bibleplanner.core.model.plan.PassageModel

/**
 * The reading a new conversation is seeded with. Sent only when the conversation is created — the
 * server freezes it, so every later turn keeps answering about the same passages.
 */
data class ChatContextModel(
    val label: String,
    val passages: List<PassageModel>,
)
