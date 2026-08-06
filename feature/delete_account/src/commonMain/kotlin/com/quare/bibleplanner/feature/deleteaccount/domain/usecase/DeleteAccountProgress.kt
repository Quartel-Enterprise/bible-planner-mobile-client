package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

sealed interface DeleteAccountProgress {
    data class InProgress(
        val phase: DeleteAccountPhase,
    ) : DeleteAccountProgress

    data class Finished(
        val result: Result<Unit>,
    ) : DeleteAccountProgress
}
