package com.quare.bibleplanner.core.chapterstudy.domain.store

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ChapterStudyScope
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ChapterStudyStatusKey
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ChapterStudyStatusPrefetchStoreTest {
    private val key = ChapterStudyStatusKey(
        userId = "user-1",
        scope = ChapterStudyScope(
            chapter = ChapterRef(
                bibleVersionId = "ACF",
                bookId = BookId.GEN,
                chapterNumber = 3,
            ),
            languageCode = "pt-BR",
        ),
    )
    private val status = ChapterStudyStatusModel(
        freeLimit = 3,
        usedCount = 1,
        isUnlocked = false,
        cacheToken = "token",
        rewardedRemainingToday = 2,
    )
    private lateinit var store: ChapterStudyStatusPrefetchStore

    @BeforeTest
    fun setUp() {
        store = ChapterStudyStatusPrefetchStore()
    }

    @Test
    fun `WHEN fetching a status THEN keeps it and returns it`() = runTest {
        // When
        val fetched = store.fetchAndKeep(key) { status }

        // Then
        assertEquals(
            expected = status,
            actual = fetched,
        )
        assertEquals(
            expected = status,
            actual = store.find(key),
        )
    }

    @Test
    fun `GIVEN the fetch finds nothing WHEN fetching THEN keeps nothing`() = runTest {
        // When
        val fetched = store.fetchAndKeep(key) { null }

        // Then
        assertNull(fetched)
        assertNull(store.find(key))
    }

    @Test
    fun `GIVEN a clear during the fetch WHEN fetching THEN returns the status without keeping it`() = runTest {
        // When
        val fetched = store.fetchAndKeep(key) {
            store.clear()
            status
        }

        // Then
        assertEquals(
            expected = status,
            actual = fetched,
        )
        assertNull(store.find(key))
    }

    @Test
    fun `GIVEN a kept status WHEN clearing THEN forgets it and keeps later fetches`() = runTest {
        // Given
        store.fetchAndKeep(key) { status }

        // When
        store.clear()
        store.fetchAndKeep(key) { status.copy(usedCount = 2) }

        // Then
        assertEquals(
            expected = status.copy(usedCount = 2),
            actual = store.find(key),
        )
    }
}
