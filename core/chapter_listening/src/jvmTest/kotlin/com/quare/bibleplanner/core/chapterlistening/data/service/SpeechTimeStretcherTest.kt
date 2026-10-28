package com.quare.bibleplanner.core.chapterlistening.data.service

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SpeechTimeStretcherTest {
    private val sampleRate = 24_000
    private val toneHz = 220.0
    private val toneSeconds = 4
    private lateinit var stretcher: SpeechTimeStretcher

    @Test
    fun `GIVEN normal speed WHEN processing samples THEN passes them through unchanged`() {
        // Given
        prepareScenario(speed = 1f)
        val samples = tone(channels = 1).copyOf(SAMPLES_PER_CHUNK)

        // When
        val output = stretcher.transform(samples)

        // Then
        assertContentEquals(samples, output)
    }

    @Test
    fun `GIVEN normal speed after some samples WHEN flushing THEN has nothing left`() {
        // Given
        prepareScenario(speed = 1f)
        stretcher.transform(tone(channels = 1).copyOf(SAMPLES_PER_CHUNK))

        // When
        val output = stretcher.takeRemaining()

        // Then
        assertEquals(0, output.size)
    }

    @Test
    fun `GIVEN three quarter speed WHEN stretching a tone THEN lasts the input length divided by the speed`() {
        // Given
        prepareScenario(speed = 0.75f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 0.75f,
        )
    }

    @Test
    fun `GIVEN one and a quarter speed WHEN stretching a tone THEN lasts the input length divided by the speed`() {
        // Given
        prepareScenario(speed = 1.25f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 1.25f,
        )
    }

    @Test
    fun `GIVEN one and a half speed WHEN stretching a tone THEN lasts the input length divided by the speed`() {
        // Given
        prepareScenario(speed = 1.5f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 1.5f,
        )
    }

    @Test
    fun `GIVEN double speed WHEN stretching a tone THEN lasts the input length divided by the speed`() {
        // Given
        prepareScenario(speed = 2f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 2f,
        )
    }

    @Test
    fun `GIVEN a speed below half WHEN stretching a tone THEN lasts the input length divided by the speed`() {
        // Given
        prepareScenario(speed = 0.4f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 0.4f,
        )
    }

    @Test
    fun `GIVEN a speed above double WHEN stretching a tone THEN lasts the input length divided by the speed`() {
        // Given
        prepareScenario(speed = 3f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 3f,
        )
    }

    @Test
    fun `GIVEN three quarter speed WHEN stretching a tone THEN keeps its pitch`() {
        // Given
        prepareScenario(speed = 0.75f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertPitchKept(output)
    }

    @Test
    fun `GIVEN one and a half speed WHEN stretching a tone THEN keeps its pitch`() {
        // Given
        prepareScenario(speed = 1.5f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertPitchKept(output)
    }

    @Test
    fun `GIVEN double speed WHEN stretching a tone THEN keeps its pitch`() {
        // Given
        prepareScenario(speed = 2f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertPitchKept(output)
    }

    @Test
    fun `GIVEN a speed below half WHEN stretching a tone THEN keeps its pitch`() {
        // Given
        prepareScenario(speed = 0.4f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertPitchKept(output)
    }

    @Test
    fun `GIVEN a speed above double WHEN stretching a tone THEN keeps its pitch`() {
        // Given
        prepareScenario(speed = 3f)
        val input = tone(channels = 1)

        // When
        val output = stretch(
            input = input,
            channels = 1,
        )

        // Then
        assertPitchKept(output)
    }

    @Test
    fun `GIVEN stereo input WHEN stretching it THEN every chunk holds whole frames`() {
        // Given
        prepareScenario(
            speed = 1.5f,
            channels = 2,
        )
        val chunks = tone(channels = 2).asList().chunked(SAMPLES_PER_CHUNK * 2).map(List<Short>::toShortArray)

        // When
        val outputs = chunks.map(stretcher::transform) + listOf(stretcher.takeRemaining())

        // Then
        assertTrue(outputs.all { output -> output.size % 2 == 0 })
    }

    @Test
    fun `GIVEN stereo input with opposite channels WHEN stretching it THEN keeps each channel in its own slot`() {
        // Given
        prepareScenario(
            speed = 1.5f,
            channels = 2,
        )
        val input = tone(channels = 2)

        // When
        val output = stretch(
            input = input,
            channels = 2,
        )

        // Then
        val left = output.filterIndexed { index, _ -> index % 2 == 0 }
        val right = output.filterIndexed { index, _ -> index % 2 == 1 }
        assertEquals(left.map { sample -> -sample }, right.map(Short::toInt))
        assertLengthMatchesSpeed(
            input = input,
            output = output,
            speed = 1.5f,
        )
    }

    // Why: the right channel is the left one inverted, so a sample landing in the wrong slot shows up.
    private fun tone(channels: Int): ShortArray {
        val frames = sampleRate * toneSeconds
        return ShortArray(frames * channels) { index ->
            val frame = index / channels
            val value = (AMPLITUDE * sin(2 * PI * toneHz * frame / sampleRate)).toInt()
            if (index % channels == 0) value.toShort() else (-value).toShort()
        }
    }

    private fun stretch(
        input: ShortArray,
        channels: Int,
    ): ShortArray {
        val chunks = input.asList().chunked(SAMPLES_PER_CHUNK * channels).map(List<Short>::toShortArray)
        val outputs = chunks.map(stretcher::transform) + listOf(stretcher.takeRemaining())
        return outputs.fold(ShortArray(0)) { stretched, output -> stretched + output }
    }

    private fun assertLengthMatchesSpeed(
        input: ShortArray,
        output: ShortArray,
        speed: Float,
    ) {
        val expected = input.size / speed
        val deviation = abs(output.size - expected) / expected
        assertTrue(deviation < LENGTH_TOLERANCE, "expected about $expected samples, got ${output.size}")
    }

    private fun assertPitchKept(output: ShortArray) {
        val risingCrossings = (1 until output.size).count { index -> output[index - 1] < 0 && output[index] >= 0 }
        val frequency = risingCrossings / (output.size.toDouble() / sampleRate)
        val deviation = abs(frequency - toneHz) / toneHz
        assertTrue(deviation < PITCH_TOLERANCE, "expected about $toneHz Hz, got $frequency Hz")
    }

    private fun prepareScenario(
        speed: Float,
        channels: Int = 1,
    ) {
        stretcher = SpeechTimeStretcher(
            sampleRate = sampleRate,
            channels = channels,
            speed = speed,
        )
    }

    private companion object {
        const val SAMPLES_PER_CHUNK = 576
        const val AMPLITUDE = 10_000
        const val LENGTH_TOLERANCE = 0.03
        const val PITCH_TOLERANCE = 0.05
    }
}
