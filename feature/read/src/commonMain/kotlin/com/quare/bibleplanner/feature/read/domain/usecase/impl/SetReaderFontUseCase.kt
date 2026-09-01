package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFont
import com.quare.bibleplanner.ui.theme.font.ReaderFont

internal class SetReaderFontUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : SetReaderFont {
    override suspend fun invoke(font: ReaderFont) {
        readerSettingsRepository.setFont(font.name)
    }
}
