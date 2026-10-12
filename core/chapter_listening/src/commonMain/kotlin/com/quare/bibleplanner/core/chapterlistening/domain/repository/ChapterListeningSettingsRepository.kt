package com.quare.bibleplanner.core.chapterlistening.domain.repository

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import kotlinx.coroutines.flow.Flow

interface ChapterListeningSettingsRepository {
    fun observe(): Flow<ChapterListeningSettingsModel>

    suspend fun setSpeed(speed: Float)

    suspend fun setAutoNextEnabled(isEnabled: Boolean)

    suspend fun setWordHighlightEnabled(isEnabled: Boolean)
}
