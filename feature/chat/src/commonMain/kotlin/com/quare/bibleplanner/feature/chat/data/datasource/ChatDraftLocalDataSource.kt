package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import kotlinx.coroutines.flow.Flow

internal interface ChatDraftLocalDataSource {
    fun observeDraft(threadKey: String): Flow<String>

    suspend fun saveDraft(draft: PendingDraftModel)

    suspend fun deleteDraft(threadKey: String)
}
