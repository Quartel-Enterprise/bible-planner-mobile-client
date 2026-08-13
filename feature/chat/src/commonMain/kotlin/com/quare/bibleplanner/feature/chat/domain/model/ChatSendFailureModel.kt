package com.quare.bibleplanner.feature.chat.domain.model

sealed interface ChatSendFailureModel {
    data object Generic : ChatSendFailureModel

    data object LimitReached : ChatSendFailureModel

    data class RateLimited(
        val retryAfterSeconds: Int,
    ) : ChatSendFailureModel
}
