package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences

class DismissBibleUpdatePromptUseCase(
    private val bibleUpdatePromptPreferences: BibleUpdatePromptPreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        bibleUpdatePromptPreferences.setLastDismissedAt(currentTimestampProvider.getCurrentTimestamp())
    }
}
