package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class FakeChatStreamCoordinator : ChatStreamCoordinator {
    private val _send: MutableStateFlow<ChatSendModel?> = MutableStateFlow(null)
    override val send: StateFlow<ChatSendModel?> = _send

    val startedRequests: MutableList<ChatSendRequestModel> = mutableListOf()
    var retryCount: Int = 0

    override fun start(request: ChatSendRequestModel) {
        startedRequests += request
        _send.value = ChatSendModel(
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
        _send.update { current -> current?.copy(failure = null) }
    }

    fun accept(conversationId: String) {
        _send.update { current ->
            current?.copy(
                conversationId = conversationId,
                isAccepted = true,
            )
        }
    }

    fun fail(failure: ChatSendFailureModel) {
        _send.update { current ->
            current?.copy(
                isStreaming = false,
                failure = failure,
            )
        }
    }
}
