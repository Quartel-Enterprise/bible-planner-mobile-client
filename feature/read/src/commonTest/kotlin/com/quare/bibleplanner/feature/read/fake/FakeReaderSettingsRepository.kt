package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeReaderSettingsRepository(
    initialSettings: ReaderSettingsModel = ReaderSettingsModel(
        fontSizeSp = ReaderFontSize.DEFAULT,
        font = ReaderFont.LORA,
        isRulerEnabled = false,
        isFocusedVerseEnabled = false,
        isVerticalReadingEnabled = false,
    ),
) : ReaderSettingsRepository {
    val settings = MutableStateFlow(initialSettings)

    override fun observe(): Flow<ReaderSettingsModel> = settings

    override suspend fun setFontSize(fontSizeSp: Float) {
        settings.value = settings.value.copy(fontSizeSp = fontSizeSp)
    }

    override suspend fun setFont(fontName: String) {
        settings.value = settings.value.copy(font = ReaderFont.valueOf(fontName))
    }

    override suspend fun setRulerEnabled(isEnabled: Boolean) {
        settings.value = settings.value.copy(isRulerEnabled = isEnabled)
    }

    override suspend fun setFocusedVerseEnabled(isEnabled: Boolean) {
        settings.value = settings.value.copy(isFocusedVerseEnabled = isEnabled)
    }

    override suspend fun setVerticalReadingEnabled(isEnabled: Boolean) {
        settings.value = settings.value.copy(isVerticalReadingEnabled = isEnabled)
    }
}
