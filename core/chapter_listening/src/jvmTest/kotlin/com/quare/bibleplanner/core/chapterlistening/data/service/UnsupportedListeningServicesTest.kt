package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration

internal class UnsupportedListeningServicesTest {
    private val audioPlayer = UnsupportedChapterAudioPlayer()
    private val mediaSession = UnsupportedListeningMediaSession()

    @Test
    fun `GIVEN a platform without an audio player WHEN playing THEN stays silent`() = runTest {
        // Given
        val audioPath = "/cache/GEN_1.mp3"

        // When
        audioPlayer.play(
            audioPath = audioPath,
            position = Duration.ZERO,
            speed = 1f,
            playbackId = 1,
        )
        audioPlayer.seekTo(Duration.ZERO)
        audioPlayer.setSpeed(1.5f)
        audioPlayer.pause()
        audioPlayer.release()

        // Then
        assertFalse(audioPlayer.isSupported)
        assertEquals(Duration.ZERO, audioPlayer.position)
        assertTrue(audioPlayer.events.toList().isEmpty())
    }

    @Test
    fun `GIVEN a platform without a media session WHEN activating it THEN refuses and sends nothing`() = runTest {
        // Given
        val nowPlaying = NowPlayingModel(
            chapterTitle = "Genesis 1",
            subtitle = "KJV",
            verseNumber = null,
            time = ListeningTimeModel(
                elapsed = Duration.ZERO,
                total = Duration.ZERO,
            ),
            isAudioLoaded = false,
            isPlaying = false,
        )

        // When
        val isActivated = mediaSession.requestActivation()
        mediaSession.update(nowPlaying)
        mediaSession.deactivate()

        // Then
        assertFalse(isActivated)
        assertTrue(mediaSession.commands.toList().isEmpty())
        assertTrue(mediaSession.interruptions.toList().isEmpty())
    }
}
