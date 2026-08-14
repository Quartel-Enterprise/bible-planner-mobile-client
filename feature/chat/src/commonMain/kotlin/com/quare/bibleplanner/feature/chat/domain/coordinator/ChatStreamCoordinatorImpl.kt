package com.quare.bibleplanner.feature.chat.domain.coordinator

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatStreamCoordinatorImpl(
    private val applicationScope: ApplicationScope,
    private val sendChatMessage: SendChatMessageUseCase,
    private val trackEvent: TrackEvent,
) : ChatStreamCoordinator {
    private val _send: MutableStateFlow<ChatSendModel?> = MutableStateFlow(null)
    override val send: StateFlow<ChatSendModel?> = _send.asStateFlow()

    private var job: Job? = null

    override fun start(request: ChatSendRequestModel) {
        if (job?.isActive == true) return
        _send.value = ChatSendModel(
            request = request,
            conversationId = request.conversationId,
            isAccepted = false,
            isStreaming = true,
            failure = null,
        )
        job = applicationScope.launch { stream(request) }
    }

    override fun retry() {
        val request = _send.value?.request ?: return
        job?.cancel()
        job = null
        start(request)
    }

    override fun clearFailure() {
        _send.update { current -> current?.copy(failure = null) }
    }

    private suspend fun stream(request: ChatSendRequestModel) {
        try {
            sendChatMessage(request).collect(::onEvent)
            _send.value = null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            val failure = sendChatMessage.mapFailure(throwable)
            if (failure == ChatSendFailureModel.ConversationGone && request.conversationId != null) {
                val newConversationRequest = request.copy(conversationId = null)
                _send.update { current ->
                    current?.copy(
                        request = newConversationRequest,
                        conversationId = null,
                    )
                }
                stream(newConversationRequest)
                return
            }
            onFailure(throwable, failure)
        }
    }

    private fun onEvent(event: ChatSendEventModel) {
        when (event) {
            is ChatSendEventModel.Accepted -> _send.update { current ->
                current?.copy(
                    conversationId = event.conversationId,
                    isAccepted = true,
                    request = current.request.copy(conversationId = event.conversationId),
                )
            }

            ChatSendEventModel.Completed -> Unit
        }
    }

    private fun onFailure(
        throwable: Throwable,
        failure: ChatSendFailureModel,
    ) {
        Logger.e(throwable) { "Chat answer failed: $failure" }
        _send.update { current ->
            current?.copy(
                isStreaming = false,
                failure = failure,
            )
        }
        trackEvent(
            name = AnalyticsEventNames.AI_CHAT_ANSWER_FAILED,
            params = mapOf(AnalyticsParams.REASON to failure.reason()),
        )
    }

    private fun ChatSendFailureModel.reason(): String = when (this) {
        ChatSendFailureModel.Generic -> GENERIC_REASON
        ChatSendFailureModel.LimitReached -> LIMIT_REACHED_REASON
        ChatSendFailureModel.ConversationGone -> CONVERSATION_GONE_REASON
        is ChatSendFailureModel.RateLimited -> RATE_LIMITED_REASON
    }

    private companion object {
        const val GENERIC_REASON = "generic"
        const val LIMIT_REACHED_REASON = "limit_reached"
        const val CONVERSATION_GONE_REASON = "conversation_gone"
        const val RATE_LIMITED_REASON = "rate_limited"
    }
}
