package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.data.dto.BookPlanDto
import com.quare.bibleplanner.core.plan.data.dto.ChaptersPlanDto
import com.quare.bibleplanner.core.plan.data.dto.DayPlanDto
import com.quare.bibleplanner.core.plan.data.dto.EndChapterDto
import com.quare.bibleplanner.core.plan.data.dto.StartChapterPlanDto
import com.quare.bibleplanner.core.plan.data.dto.WeekPlanDto
import kotlin.test.Test
import kotlin.test.assertEquals

internal class WeekPlanDtoToModelMapperTest {
    private val mapper = WeekPlanDtoToModelMapper(
        bookMapsProvider = BookMapsProvider(),
        chaptersRangeMapper = ChaptersRangeMapper(),
    )

    @Test
    fun `GIVEN a day spanning several chapters WHEN mapping THEN bounds only the first and last chapters`() {
        // Given
        val week = weekOf(
            BookPlanDto(
                chapters = ChaptersPlanDto(
                    end = EndChapterDto(
                        number = 7,
                        verse = 5,
                    ),
                    start = StartChapterPlanDto(
                        number = 5,
                        verse = 10,
                    ),
                ),
                name = "GEN",
            ),
        )

        // When
        val passage = mapper
            .map(week)
            .days
            .single()
            .passages
            .single()

        // Then
        assertEquals(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(
                    ChapterModel(
                        number = 5,
                        startVerse = 10,
                        endVerse = null,
                        bookId = BookId.GEN,
                    ),
                    ChapterModel(
                        number = 6,
                        startVerse = null,
                        endVerse = null,
                        bookId = BookId.GEN,
                    ),
                    ChapterModel(
                        number = 7,
                        startVerse = null,
                        endVerse = 5,
                        bookId = BookId.GEN,
                    ),
                ),
                isRead = false,
                chapterRanges = "5:10, 6, 7:-5",
            ),
            passage,
        )
    }

    @Test
    fun `GIVEN a day within one chapter WHEN mapping THEN keeps both verse bounds on that chapter`() {
        // Given
        val week = weekOf(
            BookPlanDto(
                chapters = ChaptersPlanDto(
                    end = EndChapterDto(
                        number = 119,
                        verse = 88,
                    ),
                    start = StartChapterPlanDto(
                        number = 119,
                        verse = 1,
                    ),
                ),
                name = "PSA",
            ),
        )

        // When
        val passage = mapper
            .map(week)
            .days
            .single()
            .passages
            .single()

        // Then
        assertEquals(
            listOf(
                ChapterModel(
                    number = 119,
                    startVerse = 1,
                    endVerse = 88,
                    bookId = BookId.PSA,
                ),
            ),
            passage.chapters,
        )
        assertEquals("119:1-88", passage.chapterRanges)
    }

    @Test
    fun `GIVEN a book without chapters WHEN mapping THEN reads the whole book`() {
        // Given
        val week = weekOf(
            BookPlanDto(
                chapters = null,
                name = "OBA",
            ),
        )

        // When
        val passage = mapper
            .map(week)
            .days
            .single()
            .passages
            .single()

        // Then
        assertEquals(
            PassageModel(
                bookId = BookId.OBA,
                chapters = emptyList(),
                isRead = false,
                chapterRanges = null,
            ),
            passage,
        )
    }

    @Test
    fun `GIVEN an unknown book name WHEN mapping THEN drops that passage but keeps the day`() {
        // Given
        val week = weekOf(
            BookPlanDto(
                chapters = null,
                name = "XYZ",
            ),
        )

        // When
        val result = mapper.map(week)

        // Then
        assertEquals(
            WeekPlanModel(
                number = 3,
                days = listOf(
                    DayModel(
                        number = 2,
                        passages = emptyList(),
                        isRead = false,
                        totalVerses = 0,
                        readVerses = 0,
                        readTimestamp = null,
                        plannedReadDate = null,
                        notes = null,
                        isToday = false,
                    ),
                ),
            ),
            result,
        )
    }

    private fun weekOf(book: BookPlanDto): WeekPlanDto = WeekPlanDto(
        days = listOf(
            DayPlanDto(
                books = listOf(book),
                day = 2,
            ),
        ),
        week = 3,
    )
}
