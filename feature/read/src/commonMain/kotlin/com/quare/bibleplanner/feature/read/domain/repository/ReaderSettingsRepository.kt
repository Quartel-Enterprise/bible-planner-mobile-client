package com.quare.bibleplanner.feature.read.domain.repository

import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import kotlinx.coroutines.flow.Flow

interface ReaderSettingsRepository {
    fun observe(): Flow<ReaderSettingsModel>

    suspend fun setFontSize(fontSizeSp: Float)

    suspend fun setFont(fontName: String)

    suspend fun setRulerEnabled(isEnabled: Boolean)

    suspend fun setRulerLines(lines: Int)

    suspend fun setFocusedVerseEnabled(isEnabled: Boolean)

    suspend fun setVerticalReadingEnabled(isEnabled: Boolean)
}
