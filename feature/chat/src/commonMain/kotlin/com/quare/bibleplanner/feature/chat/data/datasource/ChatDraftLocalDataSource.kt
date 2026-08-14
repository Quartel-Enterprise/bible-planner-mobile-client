package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.ChatDraftDao
import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ChatDraftLocalDataSource(
    private val chatDraftDao: ChatDraftDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    fun observeDraft(threadKey: String): Flow<String> = chatDraftDao
        .observeDraft(threadKey)
        .map { draft -> draft?.content.orEmpty() }
        .distinctUntilChanged()

    suspend fun deleteDraft(threadKey: String) {
        chatDraftDao.deleteDraft(threadKey)
    }

    suspend fun saveDraft(
        threadKey: String,
        content: String,
    ) {
        chatDraftDao.upsertDraft(
            ChatDraftEntity(
                threadKey = threadKey,
                content = content,
                updatedAtEpochMillis = currentTimestampProvider.getCurrentTimestamp(),
                isPendingSync = true,
            ),
        )
    }
}
