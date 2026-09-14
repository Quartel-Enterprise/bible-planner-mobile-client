package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class FakeChatStreamCoordinator : ChatStreamCoordinator {
    override val send: StateFlow<ChatSendModel?>
        field = MutableStateFlow<ChatSendModel?>(null)

    val startedRequests: MutableList<ChatSendRequestModel> = mutableListOf()
    var retryCount: Int = 0

    override fun start(request: ChatSendRequestModel) {
        startedRequests += request
        send.value = ChatSendModel(
            request = request,
            conversationId = request.conversationId,
            isAccepted = false,
            isStreaming = true,
            failure = null,
        )
    }

    override fun retry() {
        retryCount++
    }

    override fun clearFailure() {
        send.update { current -> current?.copy(failure = null) }
    }

    fun accept(conversationId: String) {
        send.update { current ->
            current?.copy(
                conversationId = conversationId,
                isAccepted = true,
            )
        }
    }

    fun fail(failure: ChatSendFailureModel) {
        send.update { current ->
            current?.copy(
                isStreaming = false,
                failure = failure,
            )
        }
    }
}
