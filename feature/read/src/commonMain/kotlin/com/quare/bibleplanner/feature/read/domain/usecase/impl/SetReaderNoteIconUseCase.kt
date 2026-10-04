package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderNoteIcon

internal class SetReaderNoteIconUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : SetReaderNoteIcon {
    override suspend fun invoke(isEnabled: Boolean) {
        readerSettingsRepository.setNoteIconEnabled(isEnabled)
    }
}
