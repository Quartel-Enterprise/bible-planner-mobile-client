package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.data.dto.BookPlanDto
import com.quare.bibleplanner.core.plan.data.dto.DayPlanDto
import com.quare.bibleplanner.core.plan.data.dto.WeekPlanDto

class WeekPlanDtoToModelMapper(
    private val bookMapsProvider: BookMapsProvider,
) {
    fun map(weekPlanDto: WeekPlanDto): WeekPlanModel = WeekPlanModel(
        number = weekPlanDto.week,
        days = weekPlanDto.days.map { dayDto ->
            mapDay(dayDto)
        },
    )

    private fun mapDay(dayDto: DayPlanDto): DayModel = DayModel(
        number = dayDto.day,
        passages = dayDto.books.mapNotNull { bookDto ->
            mapBook(bookDto)
        },
        isRead = false,
    )

    private fun mapBook(bookDto: BookPlanDto): PassagePlanModel? {
        val bookId = mapBookNameToBookId(bookDto.name) ?: return null

        val chapters = buildList {
            val startChapter = bookDto.chapters.start.number
            val endChapter = bookDto.chapters.end.number
            val startVerse = bookDto.chapters.start.verse
            val endVerse = bookDto.chapters.end.verse

            if (startChapter == endChapter) {
                // Single chapter range
                add(
                    ChapterPlanModel(
                        number = startChapter,
                        startVerse = startVerse,
                        endVerse = endVerse,
                    ),
                )
            } else {
                // Multiple chapters
                (startChapter..endChapter).forEachIndexed { index, chapterNumber ->
                    val chapterStartVerse = if (index == 0) startVerse else DEFAULT_START_VERSE
                    val chapterEndVerse = if (index == (endChapter - startChapter)) {
                        endVerse
                    } else {
                        DEFAULT_END_VERSE
                    }

                    add(
                        ChapterPlanModel(
                            number = chapterNumber,
                            startVerse = chapterStartVerse,
                            endVerse = chapterEndVerse,
                        ),
                    )
                }
            }
        }

        return PassagePlanModel(
            bookId = bookId,
            chapters = chapters,
            isRead = false,
        )
    }

    private fun mapBookNameToBookId(bookName: String): BookId? =
        bookMapsProvider.bookMaps.firstNotNullOfOrNull { it[bookName] }

    companion object {
        private const val DEFAULT_START_VERSE = 1
        private const val DEFAULT_END_VERSE = Int.MAX_VALUE // Will be updated when book data is available
    }
}
