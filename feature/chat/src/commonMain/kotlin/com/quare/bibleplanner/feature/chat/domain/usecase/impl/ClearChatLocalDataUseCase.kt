package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.clear.domain.ClearChatLocalData
import com.quare.bibleplanner.feature.chat.data.datasource.ChatLocalDataSource

internal class ClearChatLocalDataUseCase(
    private val localDataSource: ChatLocalDataSource,
) : ClearChatLocalData {
    override suspend fun invoke() {
        localDataSource.deleteAll()
    }
}
