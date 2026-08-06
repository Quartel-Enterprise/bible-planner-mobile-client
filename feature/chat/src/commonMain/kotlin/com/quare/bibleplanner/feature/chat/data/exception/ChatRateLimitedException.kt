package com.quare.bibleplanner.feature.chat.data.exception

internal class ChatRateLimitedException(
    val retryAfterSeconds: Int,
) : Exception("Chat rate limited for $retryAfterSeconds seconds")
