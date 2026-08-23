package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderVerticalReading

internal class SetReaderVerticalReadingUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : SetReaderVerticalReading {
    override suspend fun invoke(isEnabled: Boolean) {
        readerSettingsRepository.setVerticalReadingEnabled(isEnabled)
    }
}
