package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.daystudy.data.dto.ChapterRequestDto
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.feature.daystudy.data.dto.PassageRequestDto
import com.quare.bibleplanner.feature.daystudy.domain.mapper.BookIdWireNameMapper

internal class DayStudyRequestMapper(
    private val bookIdWireNameMapper: BookIdWireNameMapper,
) {
    fun map(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyRequestDto = DayStudyRequestDto(
        passages = passages.map(::mapPassage),
        version = version,
        language = languageCode,
    )

    private fun mapPassage(passage: PassageModel): PassageRequestDto = PassageRequestDto(
        book = bookIdWireNameMapper.map(passage.bookId),
        chapters = passage.chapters.map { chapter ->
            ChapterRequestDto(
                number = chapter.number,
                startVerse = chapter.startVerse,
                endVerse = chapter.endVerse,
            )
        },
    )
}
