package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.repository.ReaderSettingsRepository
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * Why: clamps on the way out so a size stored by an older build, or under older slider bounds,
 * can never render the chapter unreadable.
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
