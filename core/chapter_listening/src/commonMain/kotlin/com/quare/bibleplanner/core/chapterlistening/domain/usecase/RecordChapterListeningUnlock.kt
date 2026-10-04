package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

fun interface RecordChapterListeningUnlock {
    suspend operator fun invoke(chapter: ChapterLocationModel)
}
