package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

fun interface GetChapterListeningAccess {
    suspend operator fun invoke(chapter: ChapterLocationModel): ChapterListeningAccessModel
}

internal suspend fun GetChapterListeningAccess.isOpen(segment: ListeningSegmentModel): Boolean =
    invoke(segment.chapter) is ChapterListeningAccessModel.Open
