package com.quare.bibleplanner.feature.chat.domain.model

data class ChatQuotaModel(
    val usedCount: Int,
    val freeLimit: Int,
    val isPro: Boolean,
) {
    val remainingFree: Int
        get() = (freeLimit - usedCount).coerceAtLeast(0)

    val isExhausted: Boolean
        get() = !isPro && remainingFree == 0
}
