package com.quare.bibleplanner.core.books.presentation.mapper

import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.isNewTestament
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BookGroupMapperTest {
    private val mapper = BookGroupMapper()

    @Test
    fun `GIVEN every book WHEN grouping THEN keeps the canonical group sizes in order`() {
        // When
        val groupSizes = BookId.entries
            .map(mapper::fromBookId)
            .fold(mutableListOf<Pair<BookGroup, Int>>()) { runs, group ->
                val last = runs.lastOrNull()
                if (last?.first == group) {
                    runs[runs.lastIndex] = group to last.second + 1
                } else {
                    runs += group to 1
                }
                runs
            }

        // Then
        assertEquals(
            listOf(
                BookGroup.Pentateuch to 5,
                BookGroup.HistoricalBooks to 12,
                BookGroup.WisdomBooks to 5,
                BookGroup.MajorProphets to 5,
                BookGroup.MinorProphets to 12,
                BookGroup.Gospels to 4,
                BookGroup.Acts to 1,
                BookGroup.PaulineEpistles to 13,
                BookGroup.GeneralEpistles to 8,
                BookGroup.Revelation to 1,
            ),
            groupSizes,
        )
    }

    @Test
    fun `GIVEN every book WHEN grouping THEN the group testament matches the book testament`() {
        // When
        val testaments = BookId.entries.map { mapper.fromBookId(it).testament }

        // Then
        assertEquals(
            BookId.entries.map { if (it.isNewTestament()) BookTestament.NewTestament else BookTestament.OldTestament },
            testaments,
        )
    }

    @Test
    fun `GIVEN every group WHEN reading its title THEN each group has its own title`() {
        // When
        val titles = BookId.entries
            .map(mapper::fromBookId)
            .distinct()
            .map { it.titleRes.key }

        // Then
        assertEquals(
            listOf(
                "pentateuch",
                "historical_books",
                "wisdom_books",
                "major_prophets",
                "minor_prophets",
                "gospels",
                "acts",
                "pauline_epistles",
                "general_epistles",
                "revelation",
            ),
            titles,
        )
    }

    @Test
    fun `GIVEN each testament WHEN reading its title THEN uses its own title`() {
        // When
        val titles = BookTestament.entries.map { it.titleRes.key }

        // Then
        assertEquals(listOf("old_testament", "new_testament"), titles)
    }
}
