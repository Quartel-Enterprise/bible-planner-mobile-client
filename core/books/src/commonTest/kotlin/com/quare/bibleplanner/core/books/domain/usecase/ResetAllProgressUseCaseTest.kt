package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.books.fake.ThrowingDayDao
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class ResetAllProgressUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var dayDao: RecordingDayDao
    private lateinit var useCase: ResetAllProgressUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.RUT,
            versesPerChapter = listOf(2),
            isBookRead = true,
            readChapters = setOf(1),
            readVerses = mapOf(1 to setOf(1, 2)),
        )
        dayDao = RecordingDayDao()
        useCase = ResetAllProgressUseCase(
            dayDao = dayDao,
            bookDao = database.bookDao,
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
            currentTimestampProvider = CurrentTimestampProvider { NOW },
        )
    }

    @Test
    fun `GIVEN read progress WHEN resetting THEN clears every read flag`() = runTest {
        // When
        useCase()

        // Then
        assertFalse(database.books.any { it.isRead })
        assertFalse(database.chapters.any { it.isRead })
        assertFalse(database.verses.any { it.isRead })
    }

    @Test
    fun `GIVEN read progress WHEN resetting THEN schedules the synced datasets for push with the same timestamp`() =
        runTest {
            // When
            useCase()

            // Then
            assertEquals(listOf(NOW), dayDao.resetTimestamps)
            assertEquals(
                listOf("chapters" to NOW, "verses" to NOW),
                database.syncResets,
            )
        }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}

private class RecordingDayDao : ThrowingDayDao() {
    val resetTimestamps = mutableListOf<Long>()

    override suspend fun resetAllDayMetaForSync(now: Long) {
        resetTimestamps += now
    }
}
