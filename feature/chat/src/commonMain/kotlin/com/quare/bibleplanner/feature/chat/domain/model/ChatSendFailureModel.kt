package com.quare.bibleplanner.feature.chat.domain.model

sealed interface ChatSendFailureModel {
    /** The answer could not be obtained (network or server); the question can be sent again. */
    data object Generic : ChatSendFailureModel

    /** Free questions are over: the input locks and the paywall is one tap away. */
    data object LimitReached : ChatSendFailureModel

    /** Too many questions in a row; sending is blocked until the countdown ends. */
    data class RateLimited(
        val retryAfterSeconds: Int,
    ) : ChatSendFailureModel
}
