package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFontSize

internal class SetReaderFontSizeUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : SetReaderFontSize {
    override suspend fun invoke(fontSizeSp: Float) {
        readerSettingsRepository.setFontSize(
            fontSizeSp.coerceIn(
                minimumValue = ReaderFontSize.MIN,
                maximumValue = ReaderFontSize.MAX,
            ),
        )
    }
}
