package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AnnotatedVersionDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AnnotatedVersionDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.annotatedVersionDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN nothing marked WHEN observing the annotated versions THEN lists none`() = runTest {
        // When
        val versionIds = dao.getAnnotatedVersionIdsFlow().first()

        // Then
        assertTrue(versionIds.isEmpty())
    }

    @Test
    fun `GIVEN a highlight a bookmark and a note WHEN observing the annotated versions THEN lists each version once`() =
        runTest {
            // Given
            database.verseHighlightDao().upsertHighlights(
                listOf(
                    highlight(
                        versionId = "A21",
                        verseNumber = 4,
                        color = "yellow",
                    ),
                    highlight(
                        versionId = "A21",
                        verseNumber = 5,
                        color = "green",
                    ),
                ),
            )
            database.savedVerseDao().upsertSavedVerses(
                listOf(
                    savedVerse(
                        versionId = "NVI",
                        isSaved = true,
                    ),
                ),
            )
            database.verseNoteDao().upsertNote(
                note = note(
                    versionId = "ACF",
                    isDeleted = false,
                ),
                verses = emptyList(),
            )

            // When
            val versionIds = dao.getAnnotatedVersionIdsFlow().first()

            // Then
            assertEquals(
                expected = setOf("A21", "NVI", "ACF"),
                actual = versionIds.toSet(),
            )
            assertEquals(
                expected = 3,
                actual = versionIds.size,
            )
        }

    @Test
    fun `GIVEN only cleared marks WHEN observing the annotated versions THEN leaves their versions out`() = runTest {
        // Given
        database.verseHighlightDao().upsertHighlights(
            listOf(
                highlight(
                    versionId = "A21",
                    verseNumber = 4,
                    color = null,
                ),
            ),
        )
        database.savedVerseDao().upsertSavedVerses(
            listOf(
                savedVerse(
                    versionId = "NVI",
                    isSaved = false,
                ),
            ),
        )
        database.verseNoteDao().upsertNote(
            note = note(
                versionId = "ACF",
                isDeleted = true,
            ),
            verses = emptyList(),
        )

        // When
        val versionIds = dao.getAnnotatedVersionIdsFlow().first()

        // Then
        assertTrue(versionIds.isEmpty())
    }

    private fun highlight(
        versionId: String,
        verseNumber: Int,
        color: String?,
    ): VerseHighlightEntity = VerseHighlightEntity(
        bibleVersionId = versionId,
        bookId = BOOK_ID,
        chapterNumber = CHAPTER,
        verseNumber = verseNumber,
        color = color,
        updatedAtEpochMillis = UPDATED_AT,
        isPendingSync = false,
    )

    private fun savedVerse(
        versionId: String,
        isSaved: Boolean,
    ): SavedVerseEntity = SavedVerseEntity(
        bibleVersionId = versionId,
        bookId = BOOK_ID,
        chapterNumber = CHAPTER,
        verseNumber = 1,
        isSaved = isSaved,
        updatedAtEpochMillis = UPDATED_AT,
        isPendingSync = false,
    )

    private fun note(
        versionId: String,
        isDeleted: Boolean,
    ): VerseNoteEntity = VerseNoteEntity(
        id = "note-$versionId",
        bibleVersionId = versionId,
        bookId = BOOK_ID,
        chapterNumber = CHAPTER,
        text = "Where were you",
        isDeleted = isDeleted,
        createdAtEpochMillis = UPDATED_AT,
        updatedAtEpochMillis = UPDATED_AT,
        isPendingSync = false,
    )

    private companion object {
        const val BOOK_ID = "JOB"
        const val CHAPTER = 38
        const val UPDATED_AT = 10L
    }
}
