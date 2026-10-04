package com.quare.bibleplanner.feature.chat.data.model

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class StreamingAnswerTest {
    private val createdAt = Instant.parse("2026-08-06T15:00:00Z")

    @Test
    fun `GIVEN the answer is already cached WHEN merging THEN its id appears once`() {
        // Given
        val cached = listOf(
            message(id = "question-1"),
            message(
                id = ANSWER_ID,
                content = "Caim matou Abel por inveja.",
            ),
        )

        // When
        val merged = cached.withStreamingAnswer(
            streaming = streamingAnswer(content = "Caim matou"),
            conversationId = CONVERSATION_ID,
        )

        // Then
        assertEquals(listOf("question-1", ANSWER_ID), merged.map { it.id })
        assertEquals(1, merged.count { it.id == ANSWER_ID })
    }

    @Test
    fun `GIVEN the answer is already cached WHEN merging THEN the live text wins`() {
        // Given
        val cached = listOf(
            message(
                id = ANSWER_ID,
                content = "Caim matou Abel por inveja.",
            ),
        )

        // When
        val merged = cached.withStreamingAnswer(
            streaming = streamingAnswer(content = "Caim matou"),
            conversationId = CONVERSATION_ID,
        )

        // Then
        assertEquals("Caim matou", merged.single().content)
        assertTrue(merged.single().isStreaming)
    }

    @Test
    fun `GIVEN nothing is streaming WHEN merging THEN the thread is untouched`() {
        // Given
        val cached = listOf(message(id = "question-1"))

        // When
        val merged = cached.withStreamingAnswer(
            streaming = null,
            conversationId = CONVERSATION_ID,
        )

        // Then
        assertEquals(cached, merged)
    }

    @Test
    fun `GIVEN the answer belongs to another conversation WHEN merging THEN it is left out`() {
        // Given
        val cached = listOf(message(id = "question-1"))

        // When
        val merged = cached.withStreamingAnswer(
            streaming = streamingAnswer(content = "Caim matou"),
            conversationId = "another-conversation",
        )

        // Then
        assertEquals(cached, merged)
    }

    @Test
    fun `GIVEN the answer is not cached yet WHEN merging THEN it closes the thread`() {
        // Given
        val cached = listOf(message(id = "question-1"))

        // When
        val merged = cached.withStreamingAnswer(
            streaming = streamingAnswer(content = "Caim"),
            conversationId = CONVERSATION_ID,
        )

        // Then
        assertEquals(listOf("question-1", ANSWER_ID), merged.map { it.id })
    }

    private fun streamingAnswer(content: String): StreamingAnswer = StreamingAnswer(
        conversationId = CONVERSATION_ID,
        messageId = ANSWER_ID,
        content = content,
        createdAt = createdAt,
    )

    private fun message(
        id: String,
        content: String = "Por que Caim matou Abel?",
    ): ChatMessageModel = ChatMessageModel(
        id = id,
        role = ChatRoleModel.USER,
        content = content,
        isStreaming = false,
        isFailed = false,
        createdAt = createdAt,
    )

    private companion object {
        const val CONVERSATION_ID = "conversation-1"
        const val ANSWER_ID = "answer-1"
    }
}
