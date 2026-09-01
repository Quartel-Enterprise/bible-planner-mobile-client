package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderRulerLines

internal class SetReaderRulerLinesUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : SetReaderRulerLines {
    override suspend fun invoke(lines: Int) {
        readerSettingsRepository.setRulerLines(
            lines.coerceIn(
                minimumValue = ReaderRulerLines.MIN,
                maximumValue = ReaderRulerLines.MAX,
            ),
        )
    }
}
