package com.quare.bibleplanner.feature.chat.data.sync

import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.feature.chat.data.dto.ChatDraftDto
import com.quare.bibleplanner.feature.chat.data.mapper.ChatDraftMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChatDraftLocalStoreTest {
    private val dao = FakeChatDraftDao()
    private val store = ChatDraftLocalStore(
        chatDraftDao = dao,
        mapper = ChatDraftMapper(),
    )

    @Test
    fun `GIVEN no local row WHEN a remote draft arrives THEN it is inserted as synced`() = runTest {
        store.applyRemote(remoteDraft(content = "Por que"))

        val row = dao.rows.value[THREAD_KEY]
        assertEquals("Por que", row?.content)
        assertEquals(false, row?.isPendingSync)
    }

    @Test
    fun `GIVEN a pending local row WHEN an older remote arrives THEN the local edit survives`() = runTest {
        dao.rows.value = mapOf(
            THREAD_KEY to ChatDraftEntity(
                threadKey = THREAD_KEY,
                content = "Por que Caim",
                updatedAtEpochMillis = 2_000,
                isPendingSync = true,
            ),
        )

        store.applyRemote(
            remoteDraft(
                content = "Por",
                updatedAtEpochMillis = 1_000,
            ),
        )

        assertEquals("Por que Caim", dao.rows.value[THREAD_KEY]?.content)
        assertEquals(true, dao.rows.value[THREAD_KEY]?.isPendingSync)
    }

    @Test
    fun `GIVEN a synced local row WHEN a newer remote arrives THEN it wins`() = runTest {
        dao.rows.value = mapOf(
            THREAD_KEY to ChatDraftEntity(
                threadKey = THREAD_KEY,
                content = "Por",
                updatedAtEpochMillis = 1_000,
                isPendingSync = false,
            ),
        )

        store.applyRemote(
            remoteDraft(
                content = "Por que",
                updatedAtEpochMillis = 2_000,
            ),
        )

        assertEquals("Por que", dao.rows.value[THREAD_KEY]?.content)
    }

    @Test
    fun `GIVEN a re-touched row WHEN marking the old push synced THEN the pending flag survives`() = runTest {
        dao.rows.value = mapOf(
            THREAD_KEY to ChatDraftEntity(
                threadKey = THREAD_KEY,
                content = "Por que Caim matou",
                updatedAtEpochMillis = 3_000,
                isPendingSync = true,
            ),
        )

        store.markSynced(
            ChatDraftEntity(
                threadKey = THREAD_KEY,
                content = "Por que",
                updatedAtEpochMillis = 2_000,
                isPendingSync = true,
            ),
        )

        assertEquals(true, dao.rows.value[THREAD_KEY]?.isPendingSync)
    }

    @Test
    fun `GIVEN local state WHEN clearing THEN nothing is left`() = runTest {
        dao.rows.value = mapOf(
            THREAD_KEY to ChatDraftEntity(
                threadKey = THREAD_KEY,
                content = "Por que",
                updatedAtEpochMillis = 1_000,
                isPendingSync = true,
            ),
        )

        store.clearLocal()

        assertTrue(dao.rows.value.isEmpty())
        assertNull(dao.rows.value[THREAD_KEY])
    }

    @Test
    fun `GIVEN pending and synced rows WHEN listing what to push THEN only the pending ones are returned`() = runTest {
        // Given
        dao.rows.value = mapOf(
            THREAD_KEY to draftRow(isPendingSync = true),
            OTHER_THREAD_KEY to draftRow(isPendingSync = false).copy(threadKey = OTHER_THREAD_KEY),
        )

        // When
        val pending = store.getPending()

        // Then
        assertEquals(
            expected = listOf(THREAD_KEY),
            actual = pending.map(ChatDraftEntity::threadKey),
        )
        assertEquals(
            expected = listOf(THREAD_KEY),
            actual = store.observePending().first().map(ChatDraftEntity::threadKey),
        )
    }

    @Test
    fun `GIVEN a local row WHEN preparing it for the server THEN it carries the user and its text`() {
        // When
        val dto = store.toDto(
            userId = "user-1",
            entity = draftRow(isPendingSync = true),
        )

        // Then
        assertEquals(
            expected = "user-1",
            actual = dto.userId,
        )
        assertEquals(
            expected = THREAD_KEY,
            actual = dto.threadKey,
        )
        assertEquals(
            expected = "Por que",
            actual = dto.content,
        )
    }

    private fun draftRow(isPendingSync: Boolean): ChatDraftEntity = ChatDraftEntity(
        threadKey = THREAD_KEY,
        content = "Por que",
        updatedAtEpochMillis = 1_000,
        isPendingSync = isPendingSync,
    )

    private fun remoteDraft(
        content: String,
        updatedAtEpochMillis: Long = 1_000,
    ): ChatDraftDto = ChatDraftMapper().toDto(
        userId = "user-1",
        entity = ChatDraftEntity(
            threadKey = THREAD_KEY,
            content = content,
            updatedAtEpochMillis = updatedAtEpochMillis,
            isPendingSync = false,
        ),
    )

    private companion object {
        const val THREAD_KEY = "day:CHRONOLOGICAL:1:2"
        const val OTHER_THREAD_KEY = "conversation-1"
    }
}
