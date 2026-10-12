package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef

fun interface GetChapterVerseHeadings {
    suspend operator fun invoke(chapter: ChapterRef): Map<Int, String>
}
