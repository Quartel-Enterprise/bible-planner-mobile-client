package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.HighlightPaletteColorEntity
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseAnnotationDaosTest {
    private lateinit var database: AppDatabase

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN an older synced highlight WHEN applying a newer remote one THEN takes the remote colour`() = runTest {
        // Given
        val dao = database.verseHighlightDao()
        dao.applyRemoteHighlight(
            highlight(
                color = "yellow",
                updatedAt = 10L,
            ),
        )

        // When
        dao.applyRemoteHighlight(
            highlight(
                color = "green",
                updatedAt = 20L,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                highlight(
                    color = "green",
                    updatedAt = 20L,
                ),
            ),
            actual = dao
                .getChapterHighlightsFlow(
                    bibleVersionId = VERSION_ID,
                    bookId = BOOK_ID,
                    chapterNumber = CHAPTER,
                ).first(),
        )
    }

    @Test
    fun `GIVEN a pending highlight WHEN applying a remote one THEN keeps the local colour`() = runTest {
        // Given
        val dao = database.verseHighlightDao()
        val local = highlight(
            color = "yellow",
            updatedAt = 10L,
            isPendingSync = true,
        )
        dao.upsertHighlights(listOf(local))

        // When
        dao.applyRemoteHighlight(
            highlight(
                color = "green",
                updatedAt = 20L,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(local),
            actual = dao.getPendingSyncHighlights(),
        )
    }

    @Test
    fun `GIVEN an older synced bookmark WHEN applying a newer remote unsave THEN unsaves it`() = runTest {
        // Given
        val dao = database.savedVerseDao()
        dao.applyRemoteSavedVerse(
            savedVerse(
                isSaved = true,
                updatedAt = 10L,
            ),
        )

        // When
        dao.applyRemoteSavedVerse(
            savedVerse(
                isSaved = false,
                updatedAt = 20L,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                savedVerse(
                    isSaved = false,
                    updatedAt = 20L,
                ),
            ),
            actual = dao.getSavedVerses(
                bibleVersionId = VERSION_ID,
                bookId = BOOK_ID,
                chapterNumber = CHAPTER,
                verseNumbers = listOf(VERSE),
            ),
        )
    }

    @Test
    fun `GIVEN palette colours WHEN reading them THEN lists them from the oldest`() = runTest {
        // Given
        val dao = database.highlightPaletteColorDao()
        dao.upsertPaletteColor(
            paletteColor(
                hue = 200,
                createdAt = 2L,
            ),
        )
        dao.upsertPaletteColor(
            paletteColor(
                hue = 100,
                createdAt = 1L,
            ),
        )

        // When
        val colors = dao.getPaletteColorsFlow().first()

        // Then
        assertEquals(
            expected = listOf(100, 200),
            actual = colors.map { it.hue },
        )
    }

    private fun paletteColor(
        hue: Int,
        createdAt: Long,
    ): HighlightPaletteColorEntity = HighlightPaletteColorEntity(
        colorKey = "c:$hue:50",
        hue = hue,
        lightness = 50,
        createdAtEpochMillis = createdAt,
    )

    private fun highlight(
        color: String?,
        updatedAt: Long,
        isPendingSync: Boolean = false,
    ): VerseHighlightEntity = VerseHighlightEntity(
        bibleVersionId = VERSION_ID,
        bookId = BOOK_ID,
        chapterNumber = CHAPTER,
        verseNumber = VERSE,
        color = color,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = isPendingSync,
    )

    private fun savedVerse(
        isSaved: Boolean,
        updatedAt: Long,
    ): SavedVerseEntity = SavedVerseEntity(
        bibleVersionId = VERSION_ID,
        bookId = BOOK_ID,
        chapterNumber = CHAPTER,
        verseNumber = VERSE,
        isSaved = isSaved,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = false,
    )

    private companion object {
        const val VERSION_ID = "ACF"
        const val BOOK_ID = "JHN"
        const val CHAPTER = 3
        const val VERSE = 16
    }
}
