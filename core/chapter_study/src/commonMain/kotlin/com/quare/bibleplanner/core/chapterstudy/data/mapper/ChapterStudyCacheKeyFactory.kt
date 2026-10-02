package com.quare.bibleplanner.core.chapterstudy.data.mapper

import com.quare.bibleplanner.core.model.book.ChapterRef

internal class ChapterStudyCacheKeyFactory {
    fun create(
        chapter: ChapterRef,
        languageCode: String,
    ): String = listOf(
        chapter.bookId.name,
        chapter.chapterNumber.toString(),
        chapter.bibleVersionId,
        languageCode,
    ).joinToString(FIELD_SEPARATOR)

    private companion object {
        const val FIELD_SEPARATOR = "|"
    }
}
