package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel

fun interface GetChatSuggestions {
    suspend operator fun invoke(passages: List<PassageModel>): List<String>
}
