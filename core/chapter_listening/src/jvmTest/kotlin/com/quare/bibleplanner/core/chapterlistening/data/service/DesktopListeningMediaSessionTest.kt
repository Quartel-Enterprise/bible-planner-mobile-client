package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration

internal class DesktopListeningMediaSessionTest {
    private val mediaSession = DesktopListeningMediaSession()

    @Test
    fun `GIVEN a desktop session WHEN requesting activation THEN grants it without audio focus`() {
        // Given
        val session = mediaSession

        // When
        val isActivated = session.requestActivation()

        // Then
        assertTrue(isActivated)
    }

    @Test
    fun `GIVEN a session updated and deactivated WHEN requesting activation again THEN still grants it`() {
        // Given
        mediaSession.update(nowPlaying())
        mediaSession.deactivate()

        // When
        val isActivated = mediaSession.requestActivation()

        // Then
        assertTrue(isActivated)
    }

    @Test
    fun `GIVEN a desktop session WHEN collecting its remote commands THEN has none`() = runTest {
        // Given
        val session = mediaSession

        // When
        val commands = session.commands.toList()

        // Then
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN a desktop session WHEN collecting its interruptions THEN has none`() = runTest {
        // Given
        val session = mediaSession

        // When
        val interruptions = session.interruptions.toList()

        // Then
        assertTrue(interruptions.isEmpty())
    }

    private fun nowPlaying(): NowPlayingModel = NowPlayingModel(
        chapterTitle = "Genesis 1",
        subtitle = "KJV",
        verseNumber = 3,
        time = ListeningTimeModel(
            elapsed = Duration.ZERO,
            total = Duration.ZERO,
        ),
        isAudioLoaded = true,
        isPlaying = true,
    )
}
