package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.ChapterVerseContentModel
import com.quare.bibleplanner.core.model.book.ChapterRef

fun interface GetChapterVerseContents {
    suspend operator fun invoke(chapter: ChapterRef): Map<Int, ChapterVerseContentModel>
}
