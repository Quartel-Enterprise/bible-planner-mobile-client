package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetMaxFreeVerseNotesAmount
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ShouldBlockAddVerseNote

internal class ShouldBlockAddVerseNoteUseCase(
    private val verseNoteRepository: VerseNoteRepository,
    private val getMaxFreeVerseNotesAmount: GetMaxFreeVerseNotesAmount,
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
    private val isFreeUser: IsFreeUserUseCase,
) : ShouldBlockAddVerseNote {
    override suspend fun invoke(): Boolean = isLimitEnabled() && isFreeUser() && isMaxFreeVerseNotesReached()

    private suspend fun isLimitEnabled(): Boolean = getBooleanRemoteConfig(VERSE_NOTES_LIMIT_ENABLED_KEY)

    private suspend fun isMaxFreeVerseNotesReached(): Boolean =
        verseNoteRepository.countNotes() >= getMaxFreeVerseNotesAmount()

    companion object {
        private const val VERSE_NOTES_LIMIT_ENABLED_KEY = "verse_notes_limit_enabled"
    }
}
