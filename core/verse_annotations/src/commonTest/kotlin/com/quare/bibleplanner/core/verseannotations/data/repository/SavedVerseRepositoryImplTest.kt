package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.fake.FakeSavedVerseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SavedVerseRepositoryImplTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.JHN,
        chapterNumber = 3,
    )
    private lateinit var repository: SavedVerseRepositoryImpl
    private lateinit var dao: FakeSavedVerseDao

    @Test
    fun `observes the saved verse numbers of the chapter`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
                entity(
                    verseNumber = 17,
                    isSaved = false,
                ),
                entity(
                    verseNumber = 18,
                    isSaved = true,
                    chapterNumber = 4,
                ),
            ),
        )

        // When
        val savedVerses = repository.observeChapterSavedVerses(testChapter).first()

        // Then
        assertEquals(
            expected = setOf(16),
            actual = savedVerses,
        )
    }

    @Test
    fun `reports all saved only when every requested verse is saved`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
                entity(
                    verseNumber = 17,
                    isSaved = true,
                ),
            ),
        )

        // When
        val areAllSaved = repository.areAllSaved(listOf(verseRef(16), verseRef(17)))

        // Then
        assertTrue(areAllSaved)
    }

    @Test
    fun `reports not all saved when one verse was unsaved or never saved`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
                entity(
                    verseNumber = 17,
                    isSaved = false,
                ),
            ),
        )

        // When
        val areAllSaved = repository.areAllSaved(listOf(verseRef(16), verseRef(17), verseRef(18)))

        // Then
        assertFalse(areAllSaved)
    }

    @Test
    fun `reports not all saved for an empty selection`() = runTest {
        // Given
        prepareScenario()

        // When
        val areAllSaved = repository.areAllSaved(emptyList())

        // Then
        assertFalse(areAllSaved)
    }

    @Test
    fun `writes only the verses whose saved state changes as pending`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
            ),
        )

        // When
        repository.setSaved(
            refs = listOf(verseRef(16), verseRef(17)),
            isSaved = true,
        )

        // Then
        assertEquals(
            expected = setOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
                entity(
                    verseNumber = 17,
                    isSaved = true,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.rows.value.toSet(),
        )
    }

    @Test
    fun `unsaving keeps the row as a pending unsaved tombstone`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
            ),
        )

        // When
        repository.setSaved(
            refs = listOf(verseRef(16)),
            isSaved = false,
        )

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = false,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `writes nothing when the verses are already in the requested state`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 16,
                    isSaved = true,
                ),
            ),
        )

        // When
        repository.setSaved(
            refs = listOf(verseRef(16)),
            isSaved = true,
        )

        // Then
        assertEquals(
            expected = 0,
            actual = dao.upsertCalls,
        )
    }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        chapter = testChapter,
        verseNumber = verseNumber,
    )

    private fun entity(
        verseNumber: Int,
        isSaved: Boolean,
        updatedAt: Long = OLD_TIMESTAMP,
        isPendingSync: Boolean = false,
        chapterNumber: Int = testChapter.chapterNumber,
    ): SavedVerseEntity = SavedVerseEntity(
        bibleVersionId = testChapter.bibleVersionId,
        bookId = testChapter.bookId.name,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        isSaved = isSaved,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = isPendingSync,
    )

    private fun prepareScenario(initialRows: List<SavedVerseEntity> = emptyList()) {
        dao = FakeSavedVerseDao(initialRows = initialRows)
        repository = SavedVerseRepositoryImpl(
            savedVerseDao = dao,
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val OLD_TIMESTAMP = 100L
        const val NOW = 5_000L
    }
}
