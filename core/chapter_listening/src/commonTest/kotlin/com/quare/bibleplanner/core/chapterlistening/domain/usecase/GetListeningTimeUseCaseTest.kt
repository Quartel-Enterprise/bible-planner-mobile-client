package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class GetListeningTimeUseCaseTest {
    private val useCase = GetListeningTimeUseCase()
    private val verses = listOf(
        ListeningVerseModel(number = 1, text = "", start = Duration.ZERO, end = 6.seconds),
        ListeningVerseModel(number = 2, text = "", start = 6.seconds, end = 10.seconds),
        ListeningVerseModel(number = 3, text = "", start = 10.seconds, end = 12.seconds),
    )

    @Test
    fun `GIVEN half of the second verse heard WHEN getting the time THEN counts the verses before and half of it`() {
        // Given
        val verseProgress = 0.5f

        // When
        val time = useCase(
            verses = verses,
            verseIndex = 1,
            verseProgress = verseProgress,
            speed = 1f,
        )

        // Then
        assertEquals(
            ListeningTimeModel(
                elapsed = 8.seconds,
                total = 12.seconds,
            ),
            time,
        )
    }

    @Test
    fun `GIVEN a faster speed WHEN getting the time THEN shortens it`() {
        // Given
        val speed = 2f

        // When
        val time = useCase(
            verses = verses,
            verseIndex = 2,
            verseProgress = 0f,
            speed = speed,
        )

        // Then
        assertEquals(
            ListeningTimeModel(
                elapsed = 5.seconds,
                total = 6.seconds,
            ),
            time,
        )
    }

    @Test
    fun `GIVEN no verses WHEN getting the time THEN it is zero`() {
        // Given
        val noVerses = emptyList<ListeningVerseModel>()

        // When
        val time = useCase(
            verses = noVerses,
            verseIndex = 0,
            verseProgress = 0f,
            speed = 1f,
        )

        // Then
        assertEquals(
            ListeningTimeModel(
                elapsed = Duration.ZERO,
                total = Duration.ZERO,
            ),
            time,
        )
    }
}
