package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.RecordChapterListeningUnlock
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

internal class RecordChapterListeningUnlockUseCase(
    private val unlockRepository: ChapterListeningUnlockRepository,
    private val dateProvider: ListeningDateProvider,
) : RecordChapterListeningUnlock {
    override suspend fun invoke(chapter: ChapterLocationModel) {
        unlockRepository.addUnlockedChapter(
            date = dateProvider.today,
            chapter = chapter,
        )
    }
}
