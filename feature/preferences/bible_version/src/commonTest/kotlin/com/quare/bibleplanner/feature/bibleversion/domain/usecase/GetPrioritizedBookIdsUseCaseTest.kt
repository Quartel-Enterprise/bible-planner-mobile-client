package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetPrioritizedBookIdsUseCaseTest {
    private lateinit var useCase: GetPrioritizedBookIdsUseCase

    @BeforeTest
    fun setUp() {
        useCase = GetPrioritizedBookIdsUseCase(
            getPentateuchIds = GetPentateuchIdsUseCase(),
            getNewTestamentIds = GetNewTestamentIdsUseCase(),
        )
    }

    @Test
    fun `puts the pentateuch first then the new testament then the rest of the old testament`() {
        // When
        val bookIds = useCase()

        // Then
        assertEquals(
            expected = listOf(BookId.GEN, BookId.EXO, BookId.LEV, BookId.NUM, BookId.DEU, BookId.MAT),
            actual = bookIds.take(6),
        )
        assertEquals(
            expected = BookId.JOS,
            actual = bookIds[5 + BookId.entries.count { it.ordinal >= BookId.MAT.ordinal }],
        )
    }

    @Test
    fun `lists every book exactly once`() {
        // When
        val bookIds = useCase()

        // Then
        assertEquals(
            expected = BookId.entries.toSet(),
            actual = bookIds.toSet(),
        )
        assertEquals(
            expected = BookId.entries.size,
            actual = bookIds.size,
        )
    }
}
