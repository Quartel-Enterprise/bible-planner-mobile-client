package com.quare.bibleplanner.feature.daystudy.data.model

import com.quare.bibleplanner.feature.daystudy.data.dto.ChapterRequestDto
import com.quare.bibleplanner.feature.daystudy.data.dto.PassageRequestDto

internal data class DayStudyCacheKey(
    val passages: List<PassageRequestDto>,
    val version: String,
    val language: String,
) {
    fun asStorageKey(): String {
        val passagesPart = passages
            .sortedBy(PassageRequestDto::book)
            .joinToString(PASSAGE_SEPARATOR, transform = ::getPassageKey)
        return listOf(passagesPart, version, language).joinToString(FIELD_SEPARATOR)
    }

    private fun getPassageKey(passage: PassageRequestDto): String {
        val chaptersPart = passage.chapters
            .sortedWith(compareBy(ChapterRequestDto::number, ChapterRequestDto::startVerse))
            .joinToString(CHAPTER_SEPARATOR, transform = ::getChapterKey)
        return "${passage.book}$BOOK_CHAPTERS_SEPARATOR$chaptersPart"
    }

    private fun getChapterKey(chapter: ChapterRequestDto): String =
        if (chapter.startVerse == null && chapter.endVerse == null) {
            chapter.number.toString()
        } else {
            "${chapter.number}$VERSE_RANGE_OPEN${chapter.startVerse.orEmpty()}$VERSE_RANGE_SEPARATOR${chapter.endVerse.orEmpty()}$VERSE_RANGE_CLOSE"
        }

    private fun Int?.orEmpty(): String = this?.toString() ?: ""

    private companion object {
        const val FIELD_SEPARATOR = "|"
        const val PASSAGE_SEPARATOR = ";"
        const val CHAPTER_SEPARATOR = ","
        const val BOOK_CHAPTERS_SEPARATOR = ":"
        const val VERSE_RANGE_OPEN = "["
        const val VERSE_RANGE_SEPARATOR = "-"
        const val VERSE_RANGE_CLOSE = "]"
    }
}
