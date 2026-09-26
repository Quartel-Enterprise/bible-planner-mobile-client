package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.feature.chat.data.sync.FakeChatDraftDao
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChatDraftLocalDataSourceImplTest {
    private val now = 5_000L
    private lateinit var dao: FakeChatDraftDao
    private lateinit var dataSource: ChatDraftLocalDataSourceImpl

    @BeforeTest
    fun setUp() {
        dao = FakeChatDraftDao()
        dataSource = ChatDraftLocalDataSourceImpl(
            chatDraftDao = dao,
            currentTimestampProvider = { now },
        )
    }

    @Test
    fun `GIVEN no draft WHEN observing the thread THEN the composer starts empty`() = runTest {
        // When
        val draft = dataSource.observeDraft(THREAD_KEY).first()

        // Then
        assertEquals(
            expected = "",
            actual = draft,
        )
    }

    @Test
    fun `GIVEN typed text WHEN saving the draft THEN stores it stamped and pending sync`() = runTest {
        // When
        dataSource.saveDraft(
            PendingDraftModel(
                threadKey = THREAD_KEY,
                content = "Por que",
            ),
        )

        // Then
        assertEquals(
            expected = ChatDraftEntity(
                threadKey = THREAD_KEY,
                content = "Por que",
                updatedAtEpochMillis = now,
                isPendingSync = true,
            ),
            actual = dao.rows.value[THREAD_KEY],
        )
        assertEquals(
            expected = "Por que",
            actual = dataSource.observeDraft(THREAD_KEY).first(),
        )
    }

    @Test
    fun `GIVEN a draft WHEN only its sync flag changes THEN the composer is not re-emitted`() = runTest {
        // Given
        dataSource.saveDraft(
            PendingDraftModel(
                threadKey = THREAD_KEY,
                content = "Por que",
            ),
        )
        val emissions = mutableListOf<String>()
        backgroundScope.launch { dataSource.observeDraft(THREAD_KEY).collect { emissions += it } }
        runCurrent()

        // When
        dao.markSynced(
            threadKey = THREAD_KEY,
            syncedUpdatedAt = now,
        )
        runCurrent()

        // Then
        assertEquals(
            expected = listOf("Por que"),
            actual = emissions,
        )
    }

    @Test
    fun `GIVEN a draft WHEN deleting it THEN the thread has no draft`() = runTest {
        // Given
        dataSource.saveDraft(
            PendingDraftModel(
                threadKey = THREAD_KEY,
                content = "Por que",
            ),
        )

        // When
        dataSource.deleteDraft(THREAD_KEY)

        // Then
        assertTrue(dao.rows.value.isEmpty())
    }

    private companion object {
        const val THREAD_KEY = "conversation-1"
    }
}
