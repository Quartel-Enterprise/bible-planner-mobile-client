package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel

/**
 * The chips the chat offers before the first question: the common questions of the day's study,
 * read from its local cache. Nothing is generated here — a reading whose study was never opened
 * simply has no suggestions.
 */
fun interface GetChatSuggestions {
    suspend operator fun invoke(passages: List<PassageModel>): List<String>
}
