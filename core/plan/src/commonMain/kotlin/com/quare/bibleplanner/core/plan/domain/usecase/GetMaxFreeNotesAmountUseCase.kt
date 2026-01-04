package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService

class GetMaxFreeNotesAmountUseCase(
    private val remoteConfigService: RemoteConfigService,
) {
    suspend operator fun invoke(): Int = remoteConfigService.getInt(MAX_FREE_NOTES_KEY)

    companion object {
        private const val MAX_FREE_NOTES_KEY = "max_free_notes"
    }
}
