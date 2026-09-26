package com.quare.bibleplanner.feature.books.presentation.mapper

import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BookCategorizationMapperTest {
    private lateinit var mapper: BookCategorizationMapper

    @BeforeTest
    fun setUp() {
        mapper = BookCategorizationMapper(BookGroupMapper())
    }

    @Test
    fun `GIVEN books from both testaments WHEN mapping THEN groups them by testament and book group`() {
        // Given
        val matthew = book(BookId.MAT)
        val genesis = book(BookId.GEN)
        val romans = book(BookId.ROM)
        val exodus = book(BookId.EXO)

        // When
        val categorized = mapper.map(listOf(matthew, genesis, romans, exodus))

        // Then
        assertEquals(
            mapOf(
                BookTestament.NewTestament to listOf(
                    BookGroupPresentationModel(
                        group = BookGroup.Gospels,
                        books = listOf(matthew),
                    ),
                    BookGroupPresentationModel(
                        group = BookGroup.PaulineEpistles,
                        books = listOf(romans),
                    ),
                ),
                BookTestament.OldTestament to listOf(
                    BookGroupPresentationModel(
                        group = BookGroup.Pentateuch,
                        books = listOf(genesis, exodus),
                    ),
                ),
            ),
            categorized,
        )
    }

    @Test
    fun `GIVEN no books WHEN mapping THEN returns no testaments`() {
        // When
        val categorized = mapper.map(emptyList())

        // Then
        assertTrue(categorized.isEmpty())
    }

    private fun book(id: BookId): BookPresentationModel = BookPresentationModel(
        id = id,
        name = id.name,
        chapterProgressText = "0 / 1",
        progress = 0f,
        percentageText = "0%",
        isCompleted = false,
        isFavorite = false,
    )
}
