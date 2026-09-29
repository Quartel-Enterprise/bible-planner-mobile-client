package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetMaxFreeVerseNotesAmount

internal class GetMaxFreeVerseNotesAmountUseCase(
    private val getIntRemoteConfig: GetIntRemoteConfig,
) : GetMaxFreeVerseNotesAmount {
    override suspend fun invoke(): Int = getIntRemoteConfig(
        key = MAX_FREE_VERSE_NOTES_KEY,
        default = MAX_FREE_VERSE_NOTES_FALLBACK,
    )

    companion object {
        private const val MAX_FREE_VERSE_NOTES_KEY = "max_free_verse_notes"
        private const val MAX_FREE_VERSE_NOTES_FALLBACK = 3
    }
}
