package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.chat.data.dto.ChapterRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatContextRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.PassageRequestDto
import com.quare.bibleplanner.feature.daystudy.domain.mapper.BookIdWireNameMapper

private const val DAY_READING_TYPE = "day_reading"

internal class ChatContextRequestMapper(
    private val bookIdWireNameMapper: BookIdWireNameMapper,
) {
    fun map(
        passages: List<PassageModel>,
        version: String,
    ): ChatContextRequestDto = ChatContextRequestDto(
        type = DAY_READING_TYPE,
        version = version,
        passages = passages.map(::mapPassage),
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
