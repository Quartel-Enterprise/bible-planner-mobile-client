package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.ChatConversationEntity
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.core.provider.room.entity.ChatMessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChatDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ChatDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.chatDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN conversations WHEN replacing them THEN keeps only the new ones newest first`() = runTest {
        // Given
        dao.upsertConversations(
            listOf(
                conversation(
                    id = "gone",
                    updatedAt = 1L,
                ),
            ),
        )

        // When
        dao.replaceConversations(
            listOf(
                conversation(
                    id = "older",
                    updatedAt = 2L,
                ),
                conversation(
                    id = "newer",
                    updatedAt = 3L,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf("newer", "older"),
            actual = dao.observeConversations().first().map { it.id },
        )
    }

    @Test
    fun `GIVEN conversations WHEN replacing them with none THEN deletes them all`() = runTest {
        // Given
        dao.upsertConversations(
            listOf(
                conversation(
                    id = "gone",
                    updatedAt = 1L,
                ),
            ),
        )

        // When
        dao.replaceConversations(emptyList())

        // Then
        assertTrue(dao.observeConversations().first().isEmpty())
    }

    @Test
    fun `GIVEN messages WHEN replacing them THEN keeps the new ones with the question before its answer`() = runTest {
        // Given
        dao.upsertConversations(
            listOf(
                conversation(
                    id = CONVERSATION_ID,
                    updatedAt = 1L,
                ),
            ),
        )
        dao.upsertMessages(
            listOf(
                message(
                    id = "gone",
                    isFromUser = true,
                ),
            ),
        )

        // When
        dao.replaceMessages(
            conversationId = CONVERSATION_ID,
            messages = listOf(
                message(
                    id = "answer",
                    isFromUser = false,
                ),
                message(
                    id = "question",
                    isFromUser = true,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf("question", "answer"),
            actual = dao.observeMessages(CONVERSATION_ID).first().map { it.id },
        )
    }

    @Test
    fun `GIVEN messages WHEN replacing them with none THEN deletes the conversation messages`() = runTest {
        // Given
        dao.upsertConversations(
            listOf(
                conversation(
                    id = CONVERSATION_ID,
                    updatedAt = 1L,
                ),
            ),
        )
        dao.upsertMessages(
            listOf(
                message(
                    id = "question",
                    isFromUser = true,
                ),
            ),
        )

        // When
        dao.replaceMessages(
            conversationId = CONVERSATION_ID,
            messages = emptyList(),
        )

        // Then
        assertTrue(dao.observeMessages(CONVERSATION_ID).first().isEmpty())
    }

    @Test
    fun `GIVEN conversations WHEN deleting all THEN none is left`() = runTest {
        // Given
        dao.upsertConversations(
            listOf(
                conversation(
                    id = CONVERSATION_ID,
                    updatedAt = 1L,
                ),
            ),
        )

        // When
        dao.deleteAll()

        // Then
        assertTrue(dao.observeConversations().first().isEmpty())
    }

    @Test
    fun `GIVEN a pending draft WHEN applying a remote one THEN keeps the local draft`() = runTest {
        // Given
        val draftDao = database.chatDraftDao()
        val draft = ChatDraftEntity(
            threadKey = CONVERSATION_ID,
            content = "Local draft",
            updatedAtEpochMillis = 10L,
            isPendingSync = true,
        )
        draftDao.upsertDraft(draft)

        // When
        draftDao.applyRemoteDraft(
            threadKey = CONVERSATION_ID,
            content = "Remote draft",
            remoteUpdatedAt = 20L,
        )

        // Then
        assertEquals(
            expected = draft,
            actual = draftDao.getDraft(CONVERSATION_ID),
        )
    }

    private fun conversation(
        id: String,
        updatedAt: Long,
    ): ChatConversationEntity = ChatConversationEntity(
        id = id,
        title = "Conversation $id",
        preview = null,
        contextLabel = "Genesis 1",
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = "BOOKS",
        updatedAtEpochMillis = updatedAt,
    )

    private fun message(
        id: String,
        isFromUser: Boolean,
    ): ChatMessageEntity = ChatMessageEntity(
        id = id,
        conversationId = CONVERSATION_ID,
        isFromUser = isFromUser,
        content = "Message $id",
        isFailed = false,
        createdAtEpochMillis = 1L,
    )

    private companion object {
        const val CONVERSATION_ID = "conversation-1"
    }
}
