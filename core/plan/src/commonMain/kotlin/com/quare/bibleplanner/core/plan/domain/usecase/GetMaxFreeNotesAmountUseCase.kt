package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

class GetMaxFreeNotesAmountUseCase(
    private val getIntRemoteConfig: GetIntRemoteConfig,
) {
    suspend operator fun invoke(): Int = getIntRemoteConfig(
        key = MAX_FREE_NOTES_KEY,
        default = MAX_FREE_NOTES_FALLBACK,
    )

    companion object {
        private const val MAX_FREE_NOTES_KEY = "max_free_notes"
        private const val MAX_FREE_NOTES_FALLBACK = 3
    }
}
