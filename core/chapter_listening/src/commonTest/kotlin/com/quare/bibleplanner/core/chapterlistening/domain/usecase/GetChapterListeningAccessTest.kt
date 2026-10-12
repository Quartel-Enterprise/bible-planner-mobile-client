package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningAllowanceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GetChapterListeningAccessTest {
    private val genesisTwo = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 2,
    )
    private val segment = ListeningSegmentModel(
        chapter = genesisTwo,
        startVerse = 3,
        endVerse = 8,
    )
    private val allowance = ListeningAllowanceModel(
        hasRewardedUnlocks = true,
        hasFreeUnlocks = false,
    )

    private lateinit var getChapterListeningAccess: GetChapterListeningAccess
    private lateinit var requestedChapters: MutableList<ChapterLocationModel>

    @Test
    fun `GIVEN an open chapter WHEN checking the segment THEN it is open`() = runTest {
        // Given
        prepareScenario(access = ChapterListeningAccessModel.Open)

        // When
        val isOpen = getChapterListeningAccess.isOpen(segment)

        // Then
        assertTrue(isOpen)
        assertEquals(listOf(genesisTwo), requestedChapters)
    }

    @Test
    fun `GIVEN a chapter that needs a login WHEN checking the segment THEN it is not open`() = runTest {
        // Given
        prepareScenario(access = ChapterListeningAccessModel.LoginRequired)

        // When
        val isOpen = getChapterListeningAccess.isOpen(segment)

        // Then
        assertFalse(isOpen)
    }

    @Test
    fun `GIVEN a chapter that can be unlocked WHEN checking the segment THEN it is not open`() = runTest {
        // Given
        prepareScenario(
            access = ChapterListeningAccessModel.UnlockAvailable(
                rewardedRemainingToday = 1,
                isFreeUnlockAvailable = false,
                allowance = allowance,
            ),
        )

        // When
        val isOpen = getChapterListeningAccess.isOpen(segment)

        // Then
        assertFalse(isOpen)
    }

    @Test
    fun `GIVEN the daily limit reached WHEN checking the segment THEN it is not open`() = runTest {
        // Given
        prepareScenario(access = ChapterListeningAccessModel.LimitReached(allowance))

        // When
        val isOpen = getChapterListeningAccess.isOpen(segment)

        // Then
        assertFalse(isOpen)
    }

    private fun prepareScenario(access: ChapterListeningAccessModel) {
        requestedChapters = mutableListOf()
        getChapterListeningAccess = GetChapterListeningAccess { chapter ->
            requestedChapters += chapter
            access
        }
    }
}
