package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

fun interface GetChapterListeningAccess {
    suspend operator fun invoke(chapter: ChapterLocationModel): ChapterListeningAccessModel
}
