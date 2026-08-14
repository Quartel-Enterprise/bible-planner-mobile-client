package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.ChatDraftDao
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ChatDraftLocalDataSourceImpl(
    private val chatDraftDao: ChatDraftDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : ChatDraftLocalDataSource {
    override fun observeDraft(threadKey: String): Flow<String> = chatDraftDao
        .observeDraft(threadKey)
        .map { draft -> draft?.content.orEmpty() }
        .distinctUntilChanged()

    override suspend fun deleteDraft(threadKey: String) {
        chatDraftDao.deleteDraft(threadKey)
    }

    override suspend fun saveDraft(draft: PendingDraftModel) {
        chatDraftDao.upsertDraft(
            ChatDraftEntity(
                threadKey = draft.threadKey,
                content = draft.content,
                updatedAtEpochMillis = currentTimestampProvider.getCurrentTimestamp(),
                isPendingSync = true,
            ),
        )
    }
}
