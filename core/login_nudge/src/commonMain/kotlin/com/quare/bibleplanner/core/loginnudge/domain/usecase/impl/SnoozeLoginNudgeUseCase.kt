package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences
import com.quare.bibleplanner.core.loginnudge.domain.usecase.SnoozeLoginNudge

internal class SnoozeLoginNudgeUseCase(
    private val loginNudgePreferences: LoginNudgePreferences,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SnoozeLoginNudge {
    override suspend fun invoke() {
        loginNudgePreferences.setSnoozedAt(currentTimestampProvider.getCurrentTimestamp())
    }
}
