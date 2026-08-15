package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid

internal class SetReaderFocusAidUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : SetReaderFocusAid {
    override suspend fun invoke(focusAid: ReaderFocusAid) {
        readerSettingsRepository.setRulerEnabled(focusAid == ReaderFocusAid.RULER)
        readerSettingsRepository.setFocusedVerseEnabled(focusAid == ReaderFocusAid.FOCUSED_VERSE)
    }
}
