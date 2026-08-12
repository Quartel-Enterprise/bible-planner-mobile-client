package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.clear.domain.ClearChatLocalData
import com.quare.bibleplanner.feature.chat.data.datasource.ChatLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatStudyQuestionsLocalDataSource

internal class ClearChatLocalDataUseCase(
    private val localDataSource: ChatLocalDataSource,
    private val studyQuestionsDataSource: ChatStudyQuestionsLocalDataSource,
) : ClearChatLocalData {
    override suspend fun invoke() {
        localDataSource.deleteAll()
        studyQuestionsDataSource.clear()
    }
}
