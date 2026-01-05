package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetIntRemoteConfig

class GetMaxFreeNotesAmountUseCase(
    private val getIntRemoteConfig: GetIntRemoteConfig,
) {
    suspend operator fun invoke(): Int = getIntRemoteConfig(MAX_FREE_NOTES_KEY)

    companion object {
        private const val MAX_FREE_NOTES_KEY = "max_free_notes"
    }
}
