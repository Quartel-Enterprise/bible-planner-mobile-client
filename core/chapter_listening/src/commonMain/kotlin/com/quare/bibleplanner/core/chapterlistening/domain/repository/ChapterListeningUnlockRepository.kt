package com.quare.bibleplanner.core.chapterlistening.domain.repository

import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.datetime.LocalDate

interface ChapterListeningUnlockRepository {
    suspend fun getUnlockedChapters(date: LocalDate): Set<ChapterLocationModel>

    suspend fun addUnlockedChapter(
        date: LocalDate,
        chapter: ChapterLocationModel,
    )
}
