package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.data.exception.ChatConversationGoneException
import com.quare.bibleplanner.feature.chat.data.exception.ChatLimitReachedException
import com.quare.bibleplanner.feature.chat.data.exception.ChatRateLimitedException
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.repository.FakeChatRepository
import com.quare.bibleplanner.feature.chat.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChatStreamCoordinatorImplTest {
    private val newConversationRequest = ChatSendRequestModel(
        conversationId = null,
        message = "Por que Caim matou Abel?",
        context = null,
    )
    private val followUpRequest = newConversationRequest.copy(conversationId = "conversation-1")
    private lateinit var coordinator: ChatStreamCoordinatorImpl
    private lateinit var repository: FakeChatRepository
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @Test
    fun `GIVEN no answer in flight WHEN starting THEN streams the request`() = runTest {
        // Given
        prepareScenario(answer = { flow { awaitCancellation() } })

        // When
        coordinator.start(newConversationRequest)
        runCurrent()

        // Then
        assertEquals(
            expected = ChatSendModel(
                request = newConversationRequest,
                conversationId = null,
                isAccepted = false,
                isStreaming = true,
                failure = null,
            ),
            actual = coordinator.send.value,
        )
        assertEquals(
            expected = listOf(newConversationRequest),
            actual = repository.sentRequests,
        )
    }

    @Test
    fun `GIVEN an answer in flight WHEN starting another THEN ignores it`() = runTest {
        // Given
        prepareScenario(answer = { flow { awaitCancellation() } })
        coordinator.start(newConversationRequest)
        runCurrent()

        // When
        coordinator.start(followUpRequest)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(newConversationRequest),
            actual = repository.sentRequests,
        )
    }

    @Test
    fun `GIVEN a new conversation WHEN the server accepts it THEN follows the conversation it created`() = runTest {
        // Given
        prepareScenario(
            answer = {
                flow {
                    emit(
                        ChatSendEventModel.Accepted(
                            conversationId = "conversation-9",
                            isNewConversation = true,
                        ),
                    )
                    awaitCancellation()
                }
            },
        )

        // When
        coordinator.start(newConversationRequest)
        runCurrent()

        // Then
        val send = coordinator.send.value
        assertEquals(
            expected = "conversation-9",
            actual = send?.conversationId,
        )
        assertEquals(
            expected = "conversation-9",
            actual = send?.request?.conversationId,
        )
        assertTrue(send?.isAccepted == true)
    }

    @Test
    fun `GIVEN a streaming answer WHEN it completes THEN nothing is in flight anymore`() = runTest {
        // Given
        prepareScenario(
            answer = {
                flow {
                    emit(
                        ChatSendEventModel.Accepted(
                            conversationId = "conversation-1",
                            isNewConversation = false,
                        ),
                    )
                    emit(ChatSendEventModel.Completed)
                }
            },
        )

        // When
        coordinator.start(followUpRequest)
        runCurrent()

        // Then
        assertNull(coordinator.send.value)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN an exhausted quota WHEN the answer fails THEN reports the limit and tracks it`() = runTest {
        // Given
        prepareScenario(answer = { flow { throw ChatLimitReachedException() } })

        // When
        coordinator.start(followUpRequest)
        runCurrent()

        // Then
        val send = coordinator.send.value
        assertEquals(
            expected = ChatSendFailureModel.LimitReached,
            actual = send?.failure,
        )
        assertEquals(
            expected = false,
            actual = send?.isStreaming,
        )
        assertEquals(
            expected = listOf("ai_chat_answer_failed" to mapOf<String, Any>("reason" to "limit_reached")),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN too many questions WHEN the answer fails THEN reports the cooldown`() = runTest {
        // Given
        prepareScenario(answer = { flow { throw ChatRateLimitedException(retryAfterSeconds = 12) } })

        // When
        coordinator.start(followUpRequest)
        runCurrent()

        // Then
        assertEquals(
            expected = ChatSendFailureModel.RateLimited(retryAfterSeconds = 12),
            actual = coordinator.send.value?.failure,
        )
        assertEquals(
            expected = listOf("ai_chat_answer_failed" to mapOf<String, Any>("reason" to "rate_limited")),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN an unexpected error WHEN the answer fails THEN reports a generic failure`() = runTest {
        // Given
        prepareScenario(answer = { flow { throw IllegalStateException("boom") } })

        // When
        coordinator.start(followUpRequest)
        runCurrent()

        // Then
        assertEquals(
            expected = ChatSendFailureModel.Generic,
            actual = coordinator.send.value?.failure,
        )
        assertEquals(
            expected = listOf("ai_chat_answer_failed" to mapOf<String, Any>("reason" to "generic")),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN a deleted conversation WHEN following it up THEN asks again in a new conversation`() = runTest {
        // Given
        prepareScenario(
            answer = { request ->
                flow {
                    if (request.conversationId != null) throw ChatConversationGoneException()
                    awaitCancellation()
                }
            },
        )

        // When
        coordinator.start(followUpRequest)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(followUpRequest, newConversationRequest),
            actual = repository.sentRequests,
        )
        assertEquals(
            expected = newConversationRequest,
            actual = coordinator.send.value?.request,
        )
        assertNull(coordinator.send.value?.failure)
    }

    @Test
    fun `GIVEN a new conversation WHEN the server says it is gone THEN reports it instead of looping`() = runTest {
        // Given
        prepareScenario(answer = { flow { throw ChatConversationGoneException() } })

        // When
        coordinator.start(newConversationRequest)
        runCurrent()

        // Then
        assertEquals(
            expected = ChatSendFailureModel.ConversationGone,
            actual = coordinator.send.value?.failure,
        )
        assertEquals(
            expected = listOf("ai_chat_answer_failed" to mapOf<String, Any>("reason" to "conversation_gone")),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN a failed answer WHEN retrying THEN sends the same question again`() = runTest {
        // Given
        var attempts = 0
        prepareScenario(
            answer = {
                flow {
                    attempts++
                    if (attempts == 1) throw IllegalStateException("boom")
                    awaitCancellation()
                }
            },
        )
        coordinator.start(followUpRequest)
        runCurrent()

        // When
        coordinator.retry()
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(followUpRequest, followUpRequest),
            actual = repository.sentRequests,
        )
        assertNull(coordinator.send.value?.failure)
        assertTrue(coordinator.send.value?.isStreaming == true)
    }

    @Test
    fun `GIVEN nothing was sent WHEN retrying THEN does nothing`() = runTest {
        // Given
        prepareScenario(answer = { flow { awaitCancellation() } })

        // When
        coordinator.retry()
        runCurrent()

        // Then
        assertTrue(repository.sentRequests.isEmpty())
        assertNull(coordinator.send.value)
    }

    @Test
    fun `GIVEN a failed answer WHEN clearing the failure THEN keeps the question without the error`() = runTest {
        // Given
        prepareScenario(answer = { flow { throw IllegalStateException("boom") } })
        coordinator.start(followUpRequest)
        runCurrent()

        // When
        coordinator.clearFailure()

        // Then
        assertNull(coordinator.send.value?.failure)
        assertEquals(
            expected = followUpRequest,
            actual = coordinator.send.value?.request,
        )
    }

    private fun TestScope.prepareScenario(answer: (ChatSendRequestModel) -> Flow<ChatSendEventModel>) {
        val collectedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = collectedEvents
        repository = FakeChatRepository()
        repository.answer = answer
        coordinator = ChatStreamCoordinatorImpl(
            applicationScope = ApplicationScope(backgroundScope),
            sendChatMessage = SendChatMessageUseCase(repository),
            trackEvent = { name, params -> collectedEvents += name to params },
        )
    }
}
