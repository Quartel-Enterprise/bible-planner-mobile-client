package com.quare.bibleplanner.feature.chat.presentation.answer

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatAnswerWaitTest {
    private val recheckInterval: Duration = 5.seconds
    private val answerWait = ChatAnswerWait()
    private var messages: List<ChatMessageModel> = emptyList()
    private var messageReads = 0
    private var waitOverCount = 0

    @Test
    fun `GIVEN no messages WHEN checking THEN is not awaiting an answer`() {
        // Given
        val noMessages = emptyList<ChatMessageModel>()

        // When
        val isAwaiting = answerWait.isAwaitingAnswer(noMessages)

        // Then
        assertFalse(isAwaiting)
    }

    @Test
    fun `GIVEN the last message from the assistant WHEN checking THEN is not awaiting an answer`() {
        // Given
        val thread = listOf(
            message(
                id = "question",
                role = ChatRoleModel.USER,
                age = 1.minutes,
            ),
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                age = 1.minutes,
            ),
        )

        // When
        val isAwaiting = answerWait.isAwaitingAnswer(thread)

        // Then
        assertFalse(isAwaiting)
    }

    @Test
    fun `GIVEN a question sent within the wait window WHEN checking THEN is awaiting an answer`() {
        // Given
        val thread = listOf(
            message(
                id = "question",
                role = ChatRoleModel.USER,
                age = 1.minutes,
            ),
        )

        // When
        val isAwaiting = answerWait.isAwaitingAnswer(thread)

        // Then
        assertTrue(isAwaiting)
    }

    @Test
    fun `GIVEN a question older than the wait window WHEN checking THEN is not awaiting an answer`() {
        // Given
        val thread = listOf(
            message(
                id = "question",
                role = ChatRoleModel.USER,
                age = 3.minutes,
            ),
        )

        // When
        val isAwaiting = answerWait.isAwaitingAnswer(thread)

        // Then
        assertFalse(isAwaiting)
    }

    @Test
    fun `GIVEN an awaited answer WHEN no answer arrives THEN keeps rechecking without ending the wait`() = runTest {
        // Given
        messages = listOf(pendingQuestion())

        // When
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        advanceTimeBy(recheckInterval * 6)
        runCurrent()

        // Then
        assertEquals(7, messageReads)
        assertEquals(0, waitOverCount)
    }

    @Test
    fun `GIVEN a running watch WHEN the answer arrives THEN the wait ends once`() = runTest {
        // Given
        messages = listOf(pendingQuestion())
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()

        // When
        messages = messages + message(
            id = "answer",
            role = ChatRoleModel.ASSISTANT,
            age = Duration.ZERO,
        )
        advanceTimeBy(recheckInterval)
        runCurrent()

        // Then
        assertEquals(1, waitOverCount)
    }

    @Test
    fun `GIVEN a running watch WHEN watching again while awaiting THEN no second loop starts`() = runTest {
        // Given
        messages = listOf(pendingQuestion())
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()

        // When
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()

        // Then
        assertEquals(1, messageReads)
    }

    @Test
    fun `GIVEN a running watch WHEN no longer awaiting THEN the loop stops without ending the wait`() = runTest {
        // Given
        messages = listOf(pendingQuestion())
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()

        // When
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = false,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        advanceTimeBy(recheckInterval * 2)
        runCurrent()

        // Then
        assertEquals(1, messageReads)
        assertEquals(0, waitOverCount)
    }

    @Test
    fun `GIVEN a cancelled watch WHEN awaiting again THEN a new loop starts`() = runTest {
        // Given
        messages = listOf(pendingQuestion())
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = false,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )

        // When
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()

        // Then
        assertEquals(2, messageReads)
    }

    @Test
    fun `GIVEN messages already answered WHEN watching as awaiting THEN the wait ends right away`() = runTest {
        // Given
        messages = listOf(
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                age = Duration.ZERO,
            ),
        )

        // When
        answerWait.watch(
            scope = backgroundScope,
            isAwaitingAnswer = true,
            getMessages = ::readMessages,
            onWaitOver = ::endWait,
        )
        runCurrent()

        // Then
        assertEquals(1, waitOverCount)
    }

    private fun readMessages(): List<ChatMessageModel> {
        messageReads++
        return messages
    }

    private fun endWait() {
        waitOverCount++
    }

    private fun pendingQuestion(): ChatMessageModel = message(
        id = "question",
        role = ChatRoleModel.USER,
        age = Duration.ZERO,
    )

    private fun message(
        id: String,
        role: ChatRoleModel,
        age: Duration,
    ): ChatMessageModel = ChatMessageModel(
        id = id,
        role = role,
        content = "Por que Caim matou Abel?",
        isStreaming = false,
        isFailed = false,
        createdAt = Clock.System.now() - age,
    )
}
