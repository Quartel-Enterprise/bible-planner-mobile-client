package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseHighlightDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseHighlightRepositoryImplTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private val green = HighlightColor.Preset(PresetHighlightColor.GREEN)
    private lateinit var repository: VerseHighlightRepositoryImpl
    private lateinit var dao: FakeVerseHighlightDao

    @Test
    fun `observes the colour of each highlighted verse of the chapter`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = yellow.key,
                ),
                entity(
                    verseNumber = 2,
                    color = "c:120:50",
                ),
                entity(
                    verseNumber = 3,
                    color = null,
                ),
                entity(
                    verseNumber = 4,
                    color = "not-a-colour",
                ),
                entity(
                    verseNumber = 5,
                    color = yellow.key,
                    chapterNumber = 4,
                ),
            ),
        )

        // When
        val highlights = repository.observeChapterHighlights(testChapter).first()

        // Then
        assertEquals(
            expected = mapOf(
                1 to yellow,
                2 to HighlightColor.Custom(
                    hue = 120,
                    lightness = 50,
                ),
            ),
            actual = highlights,
        )
    }

    @Test
    fun `reads the stored colour of each requested verse and null for the rest`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = yellow.key,
                ),
                entity(
                    verseNumber = 2,
                    color = null,
                ),
            ),
        )

        // When
        val colors = repository.getColors(listOf(verseRef(1), verseRef(2), verseRef(3)))

        // Then
        assertEquals(
            expected = mapOf(
                verseRef(1) to yellow,
                verseRef(2) to null,
                verseRef(3) to null,
            ),
            actual = colors,
        )
    }

    @Test
    fun `writes the new colour as a pending change stamped with the current time`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setColor(
            refs = listOf(verseRef(1), verseRef(2)),
            color = green,
        )

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    verseNumber = 1,
                    color = green.key,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
                entity(
                    verseNumber = 2,
                    color = green.key,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `only rewrites the verses whose colour actually changes`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = green.key,
                ),
                entity(
                    verseNumber = 2,
                    color = yellow.key,
                ),
            ),
        )

        // When
        repository.setColor(
            refs = listOf(verseRef(1), verseRef(2)),
            color = green,
        )

        // Then
        assertEquals(
            expected = setOf(
                entity(
                    verseNumber = 1,
                    color = green.key,
                ),
                entity(
                    verseNumber = 2,
                    color = green.key,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.rows.value.toSet(),
        )
    }

    @Test
    fun `writes nothing when every verse already has the colour`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = green.key,
                ),
            ),
        )

        // When
        repository.setColor(
            refs = listOf(verseRef(1)),
            color = green,
        )

        // Then
        assertEquals(
            expected = 0,
            actual = dao.upsertCalls,
        )
    }

    @Test
    fun `removing a highlight leaves a pending tombstone without a colour`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = green.key,
                ),
            ),
        )

        // When
        repository.setColor(
            refs = listOf(verseRef(1)),
            color = null,
        )

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    verseNumber = 1,
                    color = null,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `clears a colour from every version that used it`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = "c:10:40",
                ),
                entity(
                    verseNumber = 2,
                    color = "c:10:40",
                    bibleVersionId = "KJV",
                ),
                entity(
                    verseNumber = 3,
                    color = yellow.key,
                ),
            ),
        )

        // When
        repository.removeAllWithColor("c:10:40")

        // Then
        assertEquals(
            expected = setOf(
                entity(
                    verseNumber = 1,
                    color = null,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
                entity(
                    verseNumber = 2,
                    color = null,
                    updatedAt = NOW,
                    isPendingSync = true,
                    bibleVersionId = "KJV",
                ),
                entity(
                    verseNumber = 3,
                    color = yellow.key,
                ),
            ),
            actual = dao.rows.value.toSet(),
        )
    }

    @Test
    fun `clearing a colour nobody used writes nothing`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    verseNumber = 1,
                    color = yellow.key,
                ),
            ),
        )

        // When
        repository.removeAllWithColor("c:10:40")

        // Then
        assertEquals(
            expected = 0,
            actual = dao.upsertCalls,
        )
        assertTrue(dao.rows.value.none { it.isPendingSync })
    }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        chapter = testChapter,
        verseNumber = verseNumber,
    )

    private fun entity(
        verseNumber: Int,
        color: String?,
        updatedAt: Long = OLD_TIMESTAMP,
        isPendingSync: Boolean = false,
        chapterNumber: Int = testChapter.chapterNumber,
        bibleVersionId: String = testChapter.bibleVersionId,
    ): VerseHighlightEntity = VerseHighlightEntity(
        bibleVersionId = bibleVersionId,
        bookId = testChapter.bookId.name,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        color = color,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = isPendingSync,
    )

    private fun prepareScenario(initialRows: List<VerseHighlightEntity> = emptyList()) {
        dao = FakeVerseHighlightDao(initialRows = initialRows)
        repository = VerseHighlightRepositoryImpl(
            verseHighlightDao = dao,
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val OLD_TIMESTAMP = 100L
        const val NOW = 5_000L
    }
}
