package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyRequestDto
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.ChapterRef

internal class ChapterStudyRequestMapper(
    private val bookIdWireNameMapper: BookIdWireNameMapper,
) {
    fun map(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyRequestDto = ChapterStudyRequestDto(
        book = bookIdWireNameMapper.map(chapter.bookId),
        chapter = chapter.chapterNumber,
        version = chapter.bibleVersionId,
        language = languageCode,
    )
}
