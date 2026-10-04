package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration

internal class UnsupportedListeningServicesTest {
    private val speechEngine = UnsupportedSpeechEngine()
    private val mediaSession = UnsupportedListeningMediaSession()

    @Test
    fun `GIVEN a platform without a voice engine WHEN asking for voices THEN has none and stays silent`() = runTest {
        // Given
        val utterance = SpeechUtteranceModel(
            id = "verse",
            text = "In the beginning",
            leadingSilence = Duration.ZERO,
        )

        // When
        speechEngine.speak(
            utterances = listOf(utterance),
            voiceId = null,
            languageTag = "en-US",
            speed = 1f,
        )
        speechEngine.stop()
        speechEngine.openVoiceSettings()

        // Then
        assertFalse(speechEngine.isSupported)
        assertEquals(SpeechVoicesModel.Unavailable, speechEngine.loadVoices("en-US"))
        assertTrue(speechEngine.events.toList().isEmpty())
    }

    @Test
    fun `GIVEN a platform without a media session WHEN activating it THEN refuses and sends nothing`() = runTest {
        // Given
        val nowPlaying = NowPlayingModel(
            chapterTitle = "Genesis 1",
            subtitle = "KJV",
            verses = emptyList(),
            verseIndex = 0,
            verseElapsed = Duration.ZERO,
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
