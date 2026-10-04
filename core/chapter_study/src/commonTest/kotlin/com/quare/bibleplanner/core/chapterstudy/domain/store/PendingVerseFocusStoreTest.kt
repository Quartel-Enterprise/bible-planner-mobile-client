package com.quare.bibleplanner.core.chapterstudy.domain.store

import com.quare.bibleplanner.core.chapterstudy.domain.model.PendingVerseFocusModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PendingVerseFocusStoreTest {
    private val focus = PendingVerseFocusModel(
        bookId = BookId.ROM,
        chapterNumber = 5,
        verseNumbers = listOf(12, 13),
    )
    private val otherFocus = PendingVerseFocusModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
        verseNumbers = listOf(15),
    )
    private lateinit var store: PendingVerseFocusStore

    @BeforeTest
    fun setUp() {
        store = PendingVerseFocusStore()
    }

    @Test
    fun `GIVEN a new store WHEN reading the pending focus THEN there is none`() {
        // When
        val pending = store.pending.value

        // Then
        assertNull(pending)
    }

    @Test
    fun `GIVEN no pending focus WHEN requesting a focus THEN it becomes the pending focus`() {
        // When
        store.request(focus)

        // Then
        assertEquals(
            expected = focus,
            actual = store.pending.value,
        )
    }

    @Test
    fun `GIVEN a pending focus WHEN requesting another THEN the latest replaces it`() {
        // Given
        store.request(focus)

        // When
        store.request(otherFocus)

        // Then
        assertEquals(
            expected = otherFocus,
            actual = store.pending.value,
        )
    }

    @Test
    fun `GIVEN a pending focus WHEN consuming it THEN nothing is pending`() {
        // Given
        store.request(focus)

        // When
        store.consume(focus)

        // Then
        assertNull(store.pending.value)
    }

    @Test
    fun `GIVEN a pending focus WHEN consuming another focus THEN the pending focus stays`() {
        // Given
        store.request(otherFocus)

        // When
        store.consume(focus)

        // Then
        assertEquals(
            expected = otherFocus,
            actual = store.pending.value,
        )
    }

    @Test
    fun `GIVEN nothing pending WHEN consuming a focus THEN nothing is pending`() {
        // When
        store.consume(focus)

        // Then
        assertNull(store.pending.value)
    }
}
