package com.quare.bibleplanner.feature.chat.data.sync

import com.quare.bibleplanner.core.provider.room.dao.ChatDraftDao
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeChatDraftDao : ChatDraftDao {
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
