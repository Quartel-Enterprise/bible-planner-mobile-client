package com.quare.bibleplanner.core.chapterlistening.data.service

import java.lang.reflect.Proxy
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.SourceDataLine
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class JavaSoundPcmOutputTest {
    private val lineFormat = AudioFormat(24_000f, 16, 1, true, false)
    private val lineFramePosition = 4_800L
    private val lineAvailableBytes = 12_000
    private lateinit var calls: MutableList<LineCall>
    private lateinit var output: JavaSoundPcmOutput

    @BeforeTest
    fun setUp() {
        calls = mutableListOf()
        output = JavaSoundPcmOutput(fakeLine())
    }

    @Test
    fun `GIVEN an open line WHEN reading the output format THEN is the line format`() {
        // When
        val format = output.format

        // Then
        assertSame(lineFormat, format)
    }

    @Test
    fun `GIVEN a line that played some frames WHEN reading the played frames THEN is the line frame position`() {
        // When
        val playedFrames = output.playedFrames

        // Then
        assertEquals(lineFramePosition, playedFrames)
    }

    @Test
    fun `GIVEN a line with free buffer space WHEN reading the writable bytes THEN is what the line has available`() {
        // When
        val writableBytes = output.writableBytes

        // Then
        assertEquals(lineAvailableBytes, writableBytes)
    }

    @Test
    fun `GIVEN some samples WHEN writing them THEN hands the same bytes range to the line`() {
        // Given
        val bytes = byteArrayOf(1, 2, 3, 4, 5, 6)

        // When
        output.write(
            bytes = bytes,
            offset = 2,
            length = 4,
        )

        // Then
        val call = calls.single()
        assertEquals("write", call.name)
        assertSame(bytes, call.args[0])
        assertContentEquals(listOf<Any?>(2, 4), call.args.drop(1))
    }

    @Test
    fun `GIVEN an open line WHEN starting the output THEN starts the line`() {
        // When
        output.start()

        // Then
        assertEquals(listOf("start"), calls.map(LineCall::name))
    }

    @Test
    fun `GIVEN a playing line WHEN stopping and discarding THEN stops the line and then flushes it`() {
        // When
        output.stopAndDiscard()

        // Then
        assertEquals(listOf("stop", "flush"), calls.map(LineCall::name))
    }

    @Test
    fun `GIVEN an open line WHEN closing the output THEN closes the line`() {
        // When
        output.close()

        // Then
        assertEquals(listOf("close"), calls.map(LineCall::name))
    }

    private fun fakeLine(): SourceDataLine = Proxy.newProxyInstance(
        SourceDataLine::class.java.classLoader,
        arrayOf(SourceDataLine::class.java),
    ) { _, method, args ->
        when (method.name) {
            "getFormat" -> lineFormat

            "getLongFramePosition" -> lineFramePosition

            "available" -> lineAvailableBytes

            "write" -> {
                calls += LineCall(method.name, args.orEmpty().toList())
                args[2]
            }

            "start", "stop", "flush", "close" -> {
                calls += LineCall(method.name, emptyList())
                null
            }

            "hashCode" -> 0

            "equals" -> false

            "toString" -> "FakeSourceDataLine"

            else -> error("unused: ${method.name}")
        }
    } as SourceDataLine
}
