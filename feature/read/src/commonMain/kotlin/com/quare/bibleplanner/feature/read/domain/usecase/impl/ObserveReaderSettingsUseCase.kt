package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Clamps the stored size on the way out so a value written by an older build — or by a slider whose
 * bounds later changed — can never render the chapter unreadable.
 */
internal class ObserveReaderSettingsUseCase(
    private val readerSettingsRepository: ReaderSettingsRepository,
) : ObserveReaderSettings {
    override fun invoke(): Flow<ReaderSettingsModel> = readerSettingsRepository.observe().map { settings ->
        settings.copy(
            fontSizeSp = settings.fontSizeSp.coerceIn(
                minimumValue = ReaderFontSize.MIN,
                maximumValue = ReaderFontSize.MAX,
            ),
        )
    }
}
