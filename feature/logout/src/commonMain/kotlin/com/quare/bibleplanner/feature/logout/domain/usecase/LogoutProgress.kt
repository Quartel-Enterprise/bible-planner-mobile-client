package com.quare.bibleplanner.feature.logout.domain.usecase

sealed interface LogoutProgress {
    data class InProgress(
        val phase: LogoutPhase,
    ) : LogoutProgress

    data class Finished(
        val result: Result<Unit>,
    ) : LogoutProgress
}
