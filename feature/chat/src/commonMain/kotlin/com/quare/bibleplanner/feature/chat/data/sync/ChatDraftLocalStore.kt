package com.quare.bibleplanner.feature.chat.data.sync

import com.quare.bibleplanner.core.provider.room.dao.ChatDraftDao
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import com.quare.bibleplanner.feature.chat.data.dto.ChatDraftDto
import com.quare.bibleplanner.feature.chat.data.mapper.ChatDraftMapper
import kotlinx.coroutines.flow.Flow

/** Adapts the composer drafts on the `chat_drafts` table to the generic sync engine. */
internal class ChatDraftLocalStore(
    private val chatDraftDao: ChatDraftDao,
    private val mapper: ChatDraftMapper,
) : SyncLocalStore<ChatDraftEntity, ChatDraftDto> {
    override fun pendingFlow(): Flow<List<ChatDraftEntity>> = chatDraftDao.getPendingSyncFlow()

    override suspend fun getPending(): List<ChatDraftEntity> = chatDraftDao.getPendingSync()

    override suspend fun markSynced(entity: ChatDraftEntity) {
        chatDraftDao.markSynced(
            threadKey = entity.threadKey,
            syncedUpdatedAt = entity.updatedAtEpochMillis,
        )
    }

    override suspend fun applyRemote(dto: ChatDraftDto) {
        val remoteUpdatedAt = mapper.toEpochMillis(dto.updatedAt)
        val updated = chatDraftDao.applyRemoteDraft(
            threadKey = dto.threadKey,
            content = dto.content,
            remoteUpdatedAt = remoteUpdatedAt,
        )
        if (updated == 0 && chatDraftDao.getDraft(dto.threadKey) == null) {
            chatDraftDao.upsertDraft(
                ChatDraftEntity(
                    threadKey = dto.threadKey,
                    content = dto.content,
                    updatedAtEpochMillis = remoteUpdatedAt,
                    isPendingSync = false,
                ),
            )
        }
    }

    override fun toDto(
        userId: String,
        entity: ChatDraftEntity,
    ): ChatDraftDto = mapper.toDto(
        userId = userId,
        entity = entity,
    )

    override suspend fun clearLocal() {
        chatDraftDao.deleteAll()
    }
}
