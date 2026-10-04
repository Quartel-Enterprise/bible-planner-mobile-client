package com.quare.bibleplanner.feature.chat.presentation.mapper

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationBucket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal class ChatConversationGroupMapperTest {
    private val now = Instant.parse("2026-08-06T15:00:00Z")
    private val mapper = ChatConversationGroupMapper()

    @Test
    fun `GIVEN conversations of different ages WHEN mapping THEN groups them by recency bucket`() {
        // Given
        val conversations = listOf(
            conversation(
                id = "today",
                updatedAt = now - 2.hours,
            ),
            conversation(
                id = "yesterday",
                updatedAt = now - 1.days,
            ),
            conversation(
                id = "recent",
                updatedAt = now - 4.days,
            ),
            conversation(
                id = "old",
                updatedAt = now - 60.days,
            ),
        )

        // When
        val groups = mapper.map(
            conversations = conversations,
            activeConversationId = null,
            query = "",
            now = now,
        )

        // Then
        val bucketsById = groups.flatMap { group -> group.conversations.map { it.id to group.bucket } }.toMap()
        assertEquals(ChatConversationBucket.Today, bucketsById.getValue("today"))
        assertEquals(ChatConversationBucket.Yesterday, bucketsById.getValue("yesterday"))
        assertEquals(ChatConversationBucket.LastSevenDays, bucketsById.getValue("recent"))
        assertTrue(bucketsById.getValue("old") is ChatConversationBucket.InMonth)
    }

    @Test
    fun `GIVEN a query WHEN mapping THEN keeps only conversations matching the title or the preview`() {
        // Given
        val conversations = listOf(
            conversation(
                id = "by-title",
                title = "Caim e Abel",
            ),
            conversation(
                id = "by-preview",
                preview = "Por que Caim matou Abel?",
            ),
            conversation(id = "unrelated"),
        )

        // When
        val groups = mapper.map(
            conversations = conversations,
            activeConversationId = null,
            query = "  caim ",
            now = now,
        )

        // Then
        assertEquals(
            listOf("by-title", "by-preview"),
            groups.flatMap { group -> group.conversations.map { it.id } },
        )
    }

    @Test
    fun `GIVEN the active conversation WHEN mapping THEN only it is marked as active`() {
        // Given
        val conversations = listOf(
            conversation(id = "first"),
            conversation(id = "second"),
        )

        // When
        val groups = mapper.map(
            conversations = conversations,
            activeConversationId = "second",
            query = "",
            now = now,
        )

        // Then
        val activeIds = groups
            .flatMap { group -> group.conversations }
            .filter { it.isActive }
            .map { it.id }
        assertEquals(listOf("second"), activeIds)
    }

    private fun conversation(
        id: String,
        title: String = "Conversa",
        preview: String? = "Uma pergunta qualquer",
        updatedAt: Instant = now,
    ): ChatConversationModel = ChatConversationModel(
        id = id,
        title = title,
        preview = preview,
        contextLabel = null,
        planDay = null,
        updatedAt = updatedAt,
    )
}
