package com.quare.bibleplanner.core.loginnudge.domain.usecase.impl

import com.quare.bibleplanner.core.loginnudge.domain.LoginNudgePreferences
import com.quare.bibleplanner.core.loginnudge.domain.usecase.DismissLoginNudgePermanently

internal class DismissLoginNudgePermanentlyUseCase(
    private val loginNudgePreferences: LoginNudgePreferences,
) : DismissLoginNudgePermanently {
    override suspend fun invoke() {
        loginNudgePreferences.setDontShowAgain()
    }
}
