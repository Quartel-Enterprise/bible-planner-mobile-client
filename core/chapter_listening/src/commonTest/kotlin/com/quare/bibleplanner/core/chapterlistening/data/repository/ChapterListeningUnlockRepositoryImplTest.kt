package com.quare.bibleplanner.core.chapterlistening.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.datastore.testing.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChapterListeningUnlockRepositoryImplTest {
    private val today = LocalDate(2026, 10, 4)
    private val tomorrow = LocalDate(2026, 10, 5)
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val johnThree = ChapterLocationModel(bookId = BookId.JHN, chapterNumber = 3)
    private lateinit var repository: ChapterListeningUnlockRepositoryImpl

    @Test
    fun `GIVEN chapters unlocked today WHEN reading today THEN returns them`() = runTest {
        // Given
        prepareScenario()
        repository.addUnlockedChapter(
            date = today,
            chapter = genesisOne,
        )
        repository.addUnlockedChapter(
            date = today,
            chapter = johnThree,
        )

        // When
        val unlocked = repository.getUnlockedChapters(today)

        // Then
        assertEquals(setOf(genesisOne, johnThree), unlocked)
    }

    @Test
    fun `GIVEN chapters unlocked yesterday WHEN reading today THEN returns none`() = runTest {
        // Given
        prepareScenario()
        repository.addUnlockedChapter(
            date = today,
            chapter = genesisOne,
        )

        // When
        val unlocked = repository.getUnlockedChapters(tomorrow)

        // Then
        assertTrue(unlocked.isEmpty())
    }

    @Test
    fun `GIVEN an unlock from another day WHEN unlocking today THEN starts the set over`() = runTest {
        // Given
        prepareScenario()
        repository.addUnlockedChapter(
            date = today,
            chapter = genesisOne,
        )

        // When
        repository.addUnlockedChapter(
            date = tomorrow,
            chapter = johnThree,
        )

        // Then
        assertEquals(setOf(johnThree), repository.getUnlockedChapters(tomorrow))
    }

    @Test
    fun `GIVEN a damaged stored entry WHEN reading THEN skips it`() = runTest {
        // Given
        prepareScenario(
            storedDate = today.toString(),
            storedChapters = setOf("GEN:1", "NOPE:2", "GEN:x"),
        )

        // When
        val unlocked = repository.getUnlockedChapters(today)

        // Then
        assertEquals(setOf(genesisOne), unlocked)
    }

    private fun prepareScenario(
        storedDate: String? = null,
        storedChapters: Set<String> = emptySet(),
    ) {
        val preferences = if (storedDate == null) {
            emptyPreferences()
        } else {
            mutablePreferencesOf(
                stringPreferencesKey("listening_unlock_date") to storedDate,
                stringSetPreferencesKey("listening_unlocked_chapters") to storedChapters,
            )
        }
        repository = ChapterListeningUnlockRepositoryImpl(FakePreferencesDataStore(preferences))
    }
}
