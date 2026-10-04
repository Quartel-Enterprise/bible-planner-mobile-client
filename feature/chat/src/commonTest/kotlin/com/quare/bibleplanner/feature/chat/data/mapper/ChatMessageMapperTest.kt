package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class ChatMessageMapperTest {
    private val mapper = ChatMessageMapper()

    @Test
    fun `GIVEN an assistant row WHEN mapping THEN role and time survive`() {
        // Given
        val row = dto(role = "assistant")

        // When
        val model = mapper.map(row)

        // Then
        assertEquals(ChatRoleModel.ASSISTANT, model.role)
        assertEquals(Instant.parse("2026-08-14T12:00:00Z"), model.createdAt)
        assertFalse(model.isStreaming)
        assertFalse(model.isFailed)
    }

    @Test
    fun `GIVEN a user row WHEN mapping THEN the role is the one of the reader`() {
        // Given
        val row = dto(role = "user")

        // When
        val model = mapper.map(row)

        // Then
        assertEquals(ChatRoleModel.USER, model.role)
    }

    @Test
    fun `GIVEN a failed answer WHEN mapping THEN the failure is kept`() {
        // Given
        val row = dto(
            role = "assistant",
            status = "failed",
        )

        // When
        val model = mapper.map(row)

        // Then
        assertTrue(model.isFailed)
    }

    private fun dto(
        role: String,
        status: String = "complete",
    ): ChatMessageDto = ChatMessageDto(
        id = "message-1",
        conversationId = "conversation-1",
        role = role,
        content = "Caim matou Abel por inveja.",
        createdAt = "2026-08-14T12:00:00Z",
        status = status,
    )
}
