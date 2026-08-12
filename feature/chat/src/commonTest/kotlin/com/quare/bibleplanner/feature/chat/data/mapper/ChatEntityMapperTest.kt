package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Instant

private val updatedAt = Instant.parse("2026-08-06T15:00:00Z")

internal class ChatEntityMapperTest {
    private val mapper = ChatEntityMapper()

    @Test
    fun `GIVEN a conversation WHEN caching and reading it back THEN nothing is lost`() {
        val conversation = ChatConversationModel(
            id = "conversation-1",
            title = "Caim e Abel",
            preview = "Por que Caim matou Abel?",
            contextLabel = "Gênesis 4-7",
            planDay = ChatPlanDayModel(
                dayNumber = 4,
                weekNumber = 1,
                readingPlanType = "CHRONOLOGICAL",
            ),
            updatedAt = updatedAt,
        )

        assertEquals(conversation, mapper.mapConversation(mapper.mapConversation(conversation)))
    }

    @Test
    fun `GIVEN a context-free conversation WHEN caching it THEN the empty fields survive`() {
        val conversation = ChatConversationModel(
            id = "conversation-2",
            title = "Uma dúvida",
            preview = null,
            contextLabel = null,
            planDay = null,
            updatedAt = updatedAt,
        )

        assertEquals(conversation, mapper.mapConversation(mapper.mapConversation(conversation)))
    }

    @Test
    fun `GIVEN a streamed answer WHEN caching it THEN it comes back as a settled message`() {
        val message = ChatMessageModel(
            id = "message-1",
            role = ChatRoleModel.ASSISTANT,
            content = "Caim matou Abel por inveja.",
            isStreaming = true,
            createdAt = updatedAt,
        )

        val cached = mapper.mapMessage(
            conversationId = "conversation-1",
            model = message,
        )

        assertEquals(message.copy(isStreaming = false), mapper.mapMessage(cached))
        assertFalse(mapper.mapMessage(cached).isStreaming)
    }

    @Test
    fun `GIVEN a question WHEN caching it THEN its author survives the round trip`() {
        val message = ChatMessageModel(
            id = "message-2",
            role = ChatRoleModel.USER,
            content = "Por que Caim matou Abel?",
            isStreaming = false,
            createdAt = updatedAt,
        )

        val cached = mapper.mapMessage(
            conversationId = "conversation-1",
            model = message,
        )

        assertEquals("conversation-1", cached.conversationId)
        assertEquals(message, mapper.mapMessage(cached))
    }
}
