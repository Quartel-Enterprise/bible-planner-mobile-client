package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.data.exception.ChatLimitReachedException
import com.quare.bibleplanner.feature.chat.data.exception.ChatRateLimitedException
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class SendChatMessageUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(request: ChatSendRequestModel): Flow<ChatSendEventModel> = repository.sendMessage(request)

    fun mapFailure(throwable: Throwable): ChatSendFailureModel = when (throwable) {
        is ChatLimitReachedException -> ChatSendFailureModel.LimitReached
        is ChatRateLimitedException -> ChatSendFailureModel.RateLimited(throwable.retryAfterSeconds)
        else -> ChatSendFailureModel.Generic
    }
}
