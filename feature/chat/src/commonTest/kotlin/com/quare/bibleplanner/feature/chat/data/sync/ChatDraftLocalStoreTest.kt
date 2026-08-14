package com.quare.bibleplanner.feature.chat.data.sync

import com.quare.bibleplanner.core.provider.room.dao.ChatDraftDao
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.feature.chat.data.dto.ChatDraftDto
import com.quare.bibleplanner.feature.chat.data.mapper.ChatDraftMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val THREAD_KEY = "day:CHRONOLOGICAL:1:2"

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
}

private class FakeChatDraftDao : ChatDraftDao {
    val rows: MutableStateFlow<Map<String, ChatDraftEntity>> = MutableStateFlow(emptyMap())

    override fun observeDraft(threadKey: String): Flow<ChatDraftEntity?> = rows
        .map { current -> current[threadKey] }

    override suspend fun upsertDraft(draft: ChatDraftEntity) {
        rows.value = rows.value + (draft.threadKey to draft)
    }

    override fun getPendingSyncFlow(): Flow<List<ChatDraftEntity>> = rows
        .map { current -> current.values.filter(ChatDraftEntity::isPendingSync) }

    override suspend fun getPendingSync(): List<ChatDraftEntity> = rows.value.values
        .filter(ChatDraftEntity::isPendingSync)

    override suspend fun markSynced(
        threadKey: String,
        syncedUpdatedAt: Long,
    ) {
        val row = rows.value[threadKey] ?: return
        if (row.updatedAtEpochMillis != syncedUpdatedAt) return
        rows.value = rows.value + (threadKey to row.copy(isPendingSync = false))
    }

    override suspend fun applyRemoteDraft(
        threadKey: String,
        content: String,
        remoteUpdatedAt: Long,
    ): Int {
        val row = rows.value[threadKey] ?: return 0
        if (row.isPendingSync || row.updatedAtEpochMillis >= remoteUpdatedAt) return 0
        rows.value = rows.value + (
            threadKey to row.copy(
                content = content,
                updatedAtEpochMillis = remoteUpdatedAt,
            )
        )
        return 1
    }

    override suspend fun getDraft(threadKey: String): ChatDraftEntity? = rows.value[threadKey]

    override suspend fun deleteDraft(threadKey: String) {
        rows.value = rows.value - threadKey
    }

    override suspend fun deleteAll() {
        rows.value = emptyMap()
    }
}
