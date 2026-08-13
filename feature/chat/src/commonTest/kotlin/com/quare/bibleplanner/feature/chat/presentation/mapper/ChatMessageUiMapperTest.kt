package com.quare.bibleplanner.feature.chat.presentation.mapper

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

private val createdAt = Instant.parse("2026-08-06T15:00:00Z")

internal class ChatMessageUiMapperTest {
    private val mapper = ChatMessageUiMapper()

    @Test
    fun `GIVEN an answer with no text yet WHEN mapping THEN it becomes the thinking state`() {
        val messages = listOf(
            message(
                id = "question",
                role = ChatRoleModel.USER,
                content = "Por que Caim matou Abel?",
            ),
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                content = "",
                isStreaming = true,
            ),
        )

        assertEquals(listOf("question"), mapper.map(messages).map { it.id })
        assertTrue(mapper.isThinking(messages))
    }

    @Test
    fun `GIVEN an answer being written WHEN mapping THEN it is shown as a streaming bubble`() {
        val messages = listOf(
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                content = "Caim matou",
                isStreaming = true,
            ),
        )

        val mapped = mapper.map(messages).single()
        assertEquals("Caim matou", mapped.text)
        assertFalse(mapped.isFromUser)
        assertTrue(mapped.isStreaming)
        assertFalse(mapper.isThinking(messages))
    }

    @Test
    fun `GIVEN a repeated id WHEN mapping THEN the list stays safe for a lazy list`() {
        val messages = listOf(
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                content = "Caim matou Abel por inveja.",
            ),
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                content = "Caim matou",
                isStreaming = true,
            ),
        )

        assertEquals(listOf("answer"), mapper.map(messages).map { it.id })
    }

    @Test
    fun `GIVEN an answer the server marked failed WHEN mapping THEN it survives to be shown`() {
        val messages = listOf(
            message(
                id = "answer",
                role = ChatRoleModel.ASSISTANT,
                content = "",
                isFailed = true,
            ),
        )

        val mapped = mapper.map(messages).single()
        assertTrue(mapped.isFailed)
        assertTrue(!mapper.isThinking(messages))
    }

    private fun message(
        id: String,
        role: ChatRoleModel,
        content: String,
        isStreaming: Boolean = false,
        isFailed: Boolean = false,
    ): ChatMessageModel = ChatMessageModel(
        id = id,
        role = role,
        content = content,
        isStreaming = isStreaming,
        isFailed = isFailed,
        createdAt = createdAt,
    )
}
