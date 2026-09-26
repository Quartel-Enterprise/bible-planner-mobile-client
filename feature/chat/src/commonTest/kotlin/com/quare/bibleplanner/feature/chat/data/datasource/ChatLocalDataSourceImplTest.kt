package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.mapper.ChatEntityMapper
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class ChatLocalDataSourceImplTest {
    private val updatedAt = Instant.parse("2026-08-14T12:00:00Z")
    private lateinit var dao: FakeChatDao
    private lateinit var dataSource: ChatLocalDataSourceImpl

    @BeforeTest
    fun setUp() {
        dao = FakeChatDao()
        dataSource = ChatLocalDataSourceImpl(
            chatDao = dao,
            mapper = ChatEntityMapper(),
        )
    }

    @Test
    fun `GIVEN a saved conversation WHEN observing the conversations THEN reads it back`() = runTest {
        // Given
        dataSource.saveConversation(conversation("conversation-1"))

        // When
        val conversations = dataSource.observeConversations().first()

        // Then
        assertEquals(
            expected = listOf(conversation("conversation-1")),
            actual = conversations,
        )
    }

    @Test
    fun `GIVEN cached conversations WHEN replacing them THEN only the new list is left`() = runTest {
        // Given
        dataSource.saveConversation(conversation("conversation-1"))

        // When
        dataSource.replaceConversations(listOf(conversation("conversation-2")))

        // Then
        assertEquals(
            expected = listOf("conversation-2"),
            actual = dataSource.observeConversations().first().map(ChatConversationModel::id),
        )
    }

    @Test
    fun `GIVEN a saved message WHEN observing its conversation THEN reads it back`() = runTest {
        // Given
        dataSource.saveMessage(
            conversationId = "conversation-1",
            message = message("question-1"),
        )

        // When
        val messages = dataSource.observeMessages("conversation-1").first()

        // Then
        assertEquals(
            expected = listOf(message("question-1")),
            actual = messages,
        )
    }

    @Test
    fun `GIVEN a cached thread WHEN replacing its messages THEN only the new messages are left`() = runTest {
        // Given
        dataSource.saveMessage(
            conversationId = "conversation-1",
            message = message("question-1"),
        )

        // When
        dataSource.replaceMessages(
            conversationId = "conversation-1",
            messages = listOf(message("question-2"), message("answer-2")),
        )

        // Then
        assertEquals(
            expected = listOf("question-2", "answer-2"),
            actual = dataSource.observeMessages("conversation-1").first().map(ChatMessageModel::id),
        )
    }

    @Test
    fun `GIVEN a cached message WHEN deleting it THEN it leaves the thread`() = runTest {
        // Given
        dataSource.saveMessage(
            conversationId = "conversation-1",
            message = message("question-1"),
        )

        // When
        dataSource.deleteMessage("question-1")

        // Then
        assertTrue(dataSource.observeMessages("conversation-1").first().isEmpty())
    }

    @Test
    fun `GIVEN a cached conversation WHEN deleting it THEN it leaves the list`() = runTest {
        // Given
        dataSource.saveConversation(conversation("conversation-1"))

        // When
        dataSource.deleteConversation("conversation-1")

        // Then
        assertTrue(dataSource.observeConversations().first().isEmpty())
    }

    @Test
    fun `GIVEN cached chats WHEN deleting everything THEN nothing is left`() = runTest {
        // Given
        dataSource.saveConversation(conversation("conversation-1"))
        dataSource.saveMessage(
            conversationId = "conversation-1",
            message = message("question-1"),
        )

        // When
        dataSource.deleteAll()

        // Then
        assertTrue(dataSource.observeConversations().first().isEmpty())
        assertTrue(dataSource.observeMessages("conversation-1").first().isEmpty())
    }

    private fun conversation(id: String): ChatConversationModel = ChatConversationModel(
        id = id,
        title = "Caim e Abel",
        preview = "Por que Caim matou Abel?",
        contextLabel = null,
        planDay = null,
        updatedAt = updatedAt,
    )

    private fun message(id: String): ChatMessageModel = ChatMessageModel(
        id = id,
        role = ChatRoleModel.USER,
        content = "Por que Caim matou Abel?",
        isStreaming = false,
        isFailed = false,
        createdAt = updatedAt,
    )
}
