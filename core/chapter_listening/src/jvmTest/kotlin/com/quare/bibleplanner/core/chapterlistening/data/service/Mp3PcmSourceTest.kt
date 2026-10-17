package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDecodeException
import java.io.File
import java.nio.file.Files
import javax.sound.sampled.AudioFormat
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class Mp3PcmSourceTest {
    private val toneFile = File(javaClass.getResource("/chapter_audio/tone_2s.mp3")!!.toURI())
    private val toneSampleRate = 24_000
    private val toneSamples = 49_536
    private lateinit var directory: File

    @BeforeTest
    fun setUp() {
        directory = Files.createTempDirectory("mp3_source").toFile()
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `GIVEN the server chapter audio WHEN opening it THEN exposes 16-bit signed little-endian mono PCM`() {
        // Given
        val path = toneFile.path

        // When
        val format = Mp3PcmSource(path = path, start = Duration.ZERO).use(Mp3PcmSource::format)

        // Then
        assertEquals(AudioFormat.Encoding.PCM_SIGNED, format.encoding)
        assertEquals(toneSampleRate.toFloat(), format.sampleRate)
        assertEquals(16, format.sampleSizeInBits)
        assertEquals(1, format.channels)
        assertFalse(format.isBigEndian)
    }

    @Test
    fun `GIVEN the start of the file WHEN reading to the end THEN decodes every sample of the tone`() {
        // Given
        val path = toneFile.path

        // When
        val samples = readAll(path = path, start = Duration.ZERO)

        // Then
        assertEquals(toneSamples, samples.size)
        assertTrue(samples.maxOf { sample -> abs(sample.toInt()) } > AUDIBLE_AMPLITUDE)
    }

    @Test
    fun `GIVEN a start of one second WHEN reading to the end THEN starts on the exact sample`() {
        // Given
        val start = 1.seconds

        // When
        val samples = readAll(path = toneFile.path, start = start)

        // Then
        assertEquals(toneSamples - toneSampleRate, samples.size)
    }

    @Test
    fun `GIVEN a start of one second WHEN reading to the end THEN matches a decode from zero`() {
        // Given
        val fromZero = readAll(path = toneFile.path, start = Duration.ZERO)

        // When
        val fromOneSecond = readAll(path = toneFile.path, start = 1.seconds)

        // Then
        assertContentEquals(fromZero.copyOfRange(toneSampleRate, fromZero.size), fromOneSecond)
    }

    @Test
    fun `GIVEN a start on a frame boundary WHEN reading to the end THEN matches a decode from zero`() {
        // Given
        val fromZero = readAll(path = toneFile.path, start = Duration.ZERO)
        val startSample = 10 * SAMPLES_PER_FRAME

        // When
        val fromBoundary = readAll(path = toneFile.path, start = 240.milliseconds)

        // Then
        assertContentEquals(fromZero.copyOfRange(startSample, fromZero.size), fromBoundary)
    }

    @Test
    fun `GIVEN a start inside the first frames WHEN reading to the end THEN matches a decode from zero`() {
        // Given
        val fromZero = readAll(path = toneFile.path, start = Duration.ZERO)
        val startSample = toneSampleRate / 20

        // When
        val fromStart = readAll(path = toneFile.path, start = 50.milliseconds)

        // Then
        assertContentEquals(fromZero.copyOfRange(startSample, fromZero.size), fromStart)
    }

    @Test
    fun `GIVEN a start past the end of the file WHEN reading THEN has nothing right away`() {
        // Given
        val start = 5.seconds

        // When
        val chunk = Mp3PcmSource(path = toneFile.path, start = start).use(Mp3PcmSource::read)

        // Then
        assertNull(chunk)
    }

    @Test
    fun `GIVEN a missing file WHEN opening it THEN fails as an unreadable chapter audio`() {
        // Given
        val path = File(directory, "missing.mp3").path

        // When
        val result = runCatching { Mp3PcmSource(path = path, start = Duration.ZERO) }

        // Then
        assertIs<ChapterAudioDecodeException>(result.exceptionOrNull())
    }

    @Test
    fun `GIVEN an empty file WHEN opening it THEN fails as an unreadable chapter audio`() {
        // Given
        val path = File(directory, "empty.mp3").apply { writeBytes(ByteArray(0)) }.path

        // When
        val result = runCatching { Mp3PcmSource(path = path, start = Duration.ZERO) }

        // Then
        assertIs<ChapterAudioDecodeException>(result.exceptionOrNull())
    }

    @Test
    fun `GIVEN a file that is not an MP3 WHEN opening it THEN fails as an unreadable chapter audio`() {
        // Given
        val path = File(directory, "page.mp3").apply { writeText("<html><body>Not found</body></html>") }.path

        // When
        val result = runCatching { Mp3PcmSource(path = path, start = Duration.ZERO) }

        // Then
        assertIs<ChapterAudioDecodeException>(result.exceptionOrNull())
    }

    private fun readAll(
        path: String,
        start: Duration,
    ): ShortArray = Mp3PcmSource(path = path, start = start).use { source ->
        generateSequence { source.read() }.fold(ShortArray(0)) { samples, chunk -> samples + chunk }
    }

    private companion object {
        const val SAMPLES_PER_FRAME = 576
        const val AUDIBLE_AMPLITUDE = 1_000
    }
}
