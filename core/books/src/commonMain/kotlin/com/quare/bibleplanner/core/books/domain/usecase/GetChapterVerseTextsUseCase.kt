package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef

class GetChapterVerseTextsUseCase(
    private val getChapterVerseContents: GetChapterVerseContents,
) : GetChapterVerseTexts {
    override suspend fun invoke(chapter: ChapterRef): Map<Int, String> =
        getChapterVerseContents(chapter).mapValues { (_, content) -> content.text }
}
