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
        val model = mapper.map(dto(role = "assistant"))

        assertEquals(ChatRoleModel.ASSISTANT, model.role)
        assertEquals(Instant.parse("2026-08-14T12:00:00Z"), model.createdAt)
        assertFalse(model.isStreaming)
        assertFalse(model.isFailed)
    }

    @Test
    fun `GIVEN a user row WHEN mapping THEN the role is the reader's`() {
        assertEquals(ChatRoleModel.USER, mapper.map(dto(role = "user")).role)
    }

    @Test
    fun `GIVEN a failed answer WHEN mapping THEN the failure is kept`() {
        assertTrue(
            mapper
                .map(
                    dto(
                        role = "assistant",
                        status = "failed",
                    ),
                ).isFailed,
        )
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
