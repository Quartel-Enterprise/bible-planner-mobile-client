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
        readVerses = 0,
        totalVerses = 0,
    )

    private fun mapBook(bookDto: BookPlanDto): PassagePlanModel? {
        val bookId = mapBookNameToBookId(bookDto.name) ?: return null

        // If chapters is null, create a passage with empty chapters list
        // This represents reading the entire book (e.g., Obadiah - single chapter book)
        val chaptersDto = bookDto.chapters ?: return PassagePlanModel(
            bookId = bookId,
            chapters = emptyList(),
            isRead = false,
        )

        val chapters = buildList {
            val startChapter = chaptersDto.start.number
            val endChapter = chaptersDto.end.number
            val startVerse = chaptersDto.start.verse
            val endVerse = chaptersDto.end.verse

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
                    val chapterStartVerse = if (index == 0) startVerse else null
                    val chapterEndVerse = if (index == (endChapter - startChapter)) {
                        endVerse
                    } else {
                        null
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
}
