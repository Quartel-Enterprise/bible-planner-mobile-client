package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.LineUnavailableException
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class DesktopChapterAudioPlayerTest {
    private val toneFile = File(javaClass.getResource("/chapter_audio/tone_2s.mp3")!!.toURI())
    private val pollStep = 20.milliseconds
    private lateinit var directory: File
    private lateinit var player: DesktopChapterAudioPlayer
    private lateinit var outputs: MutableList<FakePcmOutput>
    private lateinit var events: MutableList<ChapterAudioPlayerEvent>
    private var previousOutputCallsAtLastOpen: List<OutputCall>? = null
    private val otherFormat = AudioFormat(
        48_000f,
        16,
        1,
        true,
        false,
    )

    private val output: FakePcmOutput
        get() = outputs.last()

    @BeforeTest
    fun setUp() {
        directory = Files.createTempDirectory("desktop_player").toFile()
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `GIVEN a desktop player WHEN checking whether listening is supported THEN it is`() = runTest {
        // Given
        prepareScenario()

        // When
        val isSupported = player.isSupported

        // Then
        assertTrue(isSupported)
    }

    @Test
    fun `GIVEN nothing played yet WHEN reading the position THEN is zero`() = runTest {
        // Given
        prepareScenario()

        // When
        val position = player.position

        // Then
        assertEquals(Duration.ZERO, position)
    }

    @Test
    fun `GIVEN a play just requested WHEN reading the position before the output starts THEN is the start position`() =
        runTest {
            // Given
            prepareScenario()
            play(position = 500.milliseconds)

            // When
            val position = player.position

            // Then
            assertEquals(500.milliseconds, position)
        }

    @Test
    fun `GIVEN normal speed from half a second WHEN the output played half a second THEN the position is one second`() =
        runTest {
            // Given
            prepareScenario()
            play(position = 500.milliseconds)
            runCurrent()

            // When
            output.playedFrames = FRAME_RATE / 2L

            // Then
            assertEquals(1.seconds, player.position)
        }

    @Test
    fun `GIVEN double speed WHEN the output played one second of frames THEN the position is two seconds`() = runTest {
        // Given
        prepareScenario()
        play(speed = 2f)
        runCurrent()

        // When
        output.playedFrames = FRAME_RATE.toLong()

        // Then
        assertEquals(2.seconds, player.position)
    }

    @Test
    fun `GIVEN an output reused from an earlier play WHEN the new play advances THEN counts only its own frames`() =
        runTest {
            // Given
            prepareScenario()
            play(playbackId = 1)
            runCurrent()
            output.playedFrames = 30_000
            play(
                position = 1.seconds,
                playbackId = 2,
            )
            runCurrent()

            // When
            output.playedFrames = 30_000L + FRAME_RATE / 2

            // Then
            assertEquals(1500.milliseconds, player.position)
        }

    @Test
    fun `GIVEN a play from one second WHEN the file is decoded THEN writes only the samples after the start`() =
        runTest {
            // Given
            prepareScenario()

            // When
            play(position = 1.seconds)
            runCurrent()

            // Then
            assertEquals(TONE_FRAMES - FRAME_RATE.toLong(), output.writtenFrames)
        }

    @Test
    fun `GIVEN a play WHEN it starts THEN starts the output`() = runTest {
        // Given
        prepareScenario()

        // When
        play()
        runCurrent()

        // Then
        assertEquals(listOf(OutputCall.START), output.calls)
    }

    @Test
    fun `GIVEN every frame written WHEN the output plays all of them THEN emits Ended with the playback id`() =
        runTest {
            // Given
            prepareScenario()
            play(playbackId = 7)
            runCurrent()

            // When
            output.playedFrames = output.writtenFrames
            advanceTimeBy(pollStep)

            // Then
            assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(7)), events)
        }

    @Test
    fun `GIVEN a chapter that ended WHEN more time passes THEN emits Ended only once`() = runTest {
        // Given
        prepareScenario()
        play(playbackId = 7)
        runCurrent()
        output.playedFrames = output.writtenFrames
        advanceTimeBy(pollStep)

        // When
        advanceTimeBy(5.seconds)

        // Then
        assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(7)), events)
    }

    @Test
    fun `GIVEN an output still advancing WHEN it has not played every frame THEN emits nothing`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()

        // When
        repeat(30) {
            output.playedFrames += 1_000
            advanceTimeBy(100.milliseconds)
        }

        // Then
        assertTrue(events.isEmpty())
    }

    @Test
    fun `GIVEN an output stopped a few frames short of the end WHEN under the stall timeout THEN emits nothing`() =
        runTest {
            // Given
            prepareScenario()
            play()
            runCurrent()

            // When
            output.playedFrames = output.writtenFrames - 100
            advanceTimeBy(1500.milliseconds)

            // Then
            assertTrue(events.isEmpty())
        }

    @Test
    fun `GIVEN an output stopped a few frames short of the end WHEN past the stall timeout THEN emits Ended`() =
        runTest {
            // Given
            prepareScenario()
            play(playbackId = 3)
            runCurrent()

            // When
            output.playedFrames = output.writtenFrames - 100
            advanceTimeBy(2500.milliseconds)

            // Then
            assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(3)), events)
        }

    @Test
    fun `GIVEN a playing chapter WHEN pausing THEN keeps the position it reached`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()
        output.playedFrames = FRAME_RATE / 2L

        // When
        player.pause()
        output.playedFrames = 30_000

        // Then
        assertEquals(500.milliseconds, player.position)
    }

    @Test
    fun `GIVEN a playing chapter WHEN pausing THEN stops and discards the output`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()

        // When
        player.pause()

        // Then
        assertEquals(listOf(OutputCall.START, OutputCall.STOP_AND_DISCARD), output.calls)
    }

    @Test
    fun `GIVEN a paused chapter WHEN the output plays out what it had THEN emits nothing`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()
        player.pause()

        // When
        output.playedFrames = output.writtenFrames
        advanceTimeBy(3.seconds)

        // Then
        assertTrue(events.isEmpty())
    }

    @Test
    fun `GIVEN a paused chapter WHEN seeking THEN moves the position`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()
        player.pause()

        // When
        player.seekTo(1500.milliseconds)
        runCurrent()

        // Then
        assertEquals(1500.milliseconds, player.position)
    }

    @Test
    fun `GIVEN a paused chapter WHEN seeking THEN does not play again`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()
        player.pause()
        val writtenBeforeSeek = output.writtenFrames

        // When
        player.seekTo(1500.milliseconds)
        advanceTimeBy(3.seconds)

        // Then
        assertEquals(writtenBeforeSeek, output.writtenFrames)
        assertEquals(listOf(OutputCall.START, OutputCall.STOP_AND_DISCARD), output.calls)
    }

    @Test
    fun `GIVEN a playing chapter WHEN seeking THEN plays again from the new position`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()
        val writtenBeforeSeek = output.writtenFrames

        // When
        player.seekTo(1.seconds)
        runCurrent()

        // Then
        assertEquals(TONE_FRAMES - FRAME_RATE, output.writtenFrames - writtenBeforeSeek)
        assertEquals(1.seconds, player.position)
    }

    @Test
    fun `GIVEN nothing loaded WHEN seeking THEN does nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        player.seekTo(1.seconds)
        runCurrent()

        // Then
        assertEquals(Duration.ZERO, player.position)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun `GIVEN a playing chapter WHEN changing the speed THEN writes the rest of the file stretched by the speed`() =
        runTest {
            // Given
            prepareScenario()
            play()
            runCurrent()
            output.playedFrames = FRAME_RATE / 2L
            val writtenBeforeChange = output.writtenFrames

            // When
            player.setSpeed(2f)
            runCurrent()

            // Then
            val expected = (TONE_FRAMES - FRAME_RATE / 2) / 2.0
            val written = output.writtenFrames - writtenBeforeChange
            assertTrue(abs(written - expected) / expected < LENGTH_TOLERANCE, "expected about $expected, got $written")
        }

    @Test
    fun `GIVEN a playing chapter WHEN changing the speed THEN continues from the same position at the new speed`() =
        runTest {
            // Given
            prepareScenario()
            play()
            runCurrent()
            output.playedFrames = FRAME_RATE / 2L

            // When
            player.setSpeed(2f)
            runCurrent()
            output.playedFrames += FRAME_RATE / 2

            // Then
            assertEquals(1500.milliseconds, player.position)
        }

    @Test
    fun `GIVEN a playing chapter WHEN setting the same speed THEN does nothing`() = runTest {
        // Given
        prepareScenario()
        play(speed = 1.5f)
        runCurrent()
        val writtenBeforeChange = output.writtenFrames

        // When
        player.setSpeed(1.5f)
        runCurrent()

        // Then
        assertEquals(writtenBeforeChange, output.writtenFrames)
        assertEquals(listOf(OutputCall.START), output.calls)
    }

    @Test
    fun `GIVEN a chapter played before WHEN playing again with the same format THEN reuses the output`() = runTest {
        // Given
        prepareScenario()
        play(playbackId = 1)
        runCurrent()

        // When
        play(playbackId = 2)
        runCurrent()

        // Then
        assertEquals(1, outputs.size)
    }

    @Test
    fun `GIVEN a missing file WHEN playing THEN emits Failed as an unreadable file`() = runTest {
        // Given
        prepareScenario()
        val path = File(directory, "missing.mp3").path

        // When
        play(
            path = path,
            playbackId = 4,
        )
        runCurrent()

        // Then
        assertEquals(
            listOf<ChapterAudioPlayerEvent>(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = 4,
                    isFileUnreadable = true,
                ),
            ),
            events,
        )
    }

    @Test
    fun `GIVEN a corrupt file WHEN playing THEN emits Failed as an unreadable file`() = runTest {
        // Given
        prepareScenario()
        val path = File(directory, "corrupt.mp3").apply { writeText("<html><body>Not found</body></html>") }.path

        // When
        play(
            path = path,
            playbackId = 4,
        )
        runCurrent()

        // Then
        assertEquals(
            listOf<ChapterAudioPlayerEvent>(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = 4,
                    isFileUnreadable = true,
                ),
            ),
            events,
        )
    }

    @Test
    fun `GIVEN an audio output that cannot open WHEN playing THEN emits Failed not blaming the file`() = runTest {
        // Given
        prepareScenario(failingOpens = 1)

        // When
        play(playbackId = 5)
        runCurrent()

        // Then
        assertEquals(
            listOf<ChapterAudioPlayerEvent>(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = 5,
                    isFileUnreadable = false,
                ),
            ),
            events,
        )
    }

    @Test
    fun `GIVEN an audio output that failed to open WHEN playing again THEN opens a new output and plays`() = runTest {
        // Given
        prepareScenario(failingOpens = 1)
        play(playbackId = 5)
        runCurrent()

        // When
        play(playbackId = 6)
        runCurrent()

        // Then
        assertEquals(TONE_FRAMES, outputs.single().writtenFrames)
    }

    @Test
    fun `GIVEN a playing chapter WHEN releasing THEN closes the output`() = runTest {
        // Given
        prepareScenario()
        play()
        runCurrent()

        // When
        player.release()

        // Then
        assertEquals(OutputCall.CLOSE, output.calls.last())
    }

    @Test
    fun `GIVEN a playing chapter past its start WHEN releasing THEN resets the position to zero`() = runTest {
        // Given
        prepareScenario()
        play(position = 1.seconds)
        runCurrent()
        output.playedFrames = FRAME_RATE / 2L

        // When
        player.release()

        // Then
        assertEquals(Duration.ZERO, player.position)
    }

    @Test
    fun `GIVEN a released player WHEN playing again THEN opens a new output`() = runTest {
        // Given
        prepareScenario()
        play(playbackId = 1)
        runCurrent()
        player.release()

        // When
        play(playbackId = 2)
        runCurrent()

        // Then
        assertEquals(2, outputs.size)
        assertEquals(TONE_FRAMES, outputs.last().writtenFrames)
    }

    @Test
    fun `GIVEN a run waiting for room in the output WHEN pausing and room frees up THEN writes nothing more`() =
        runTest {
            // Given
            prepareScenario(writableBytes = 0)
            play()
            runCurrent()

            // When
            player.pause()
            output.writableBytes = Int.MAX_VALUE
            advanceTimeBy(3.seconds)

            // Then
            assertEquals(0, output.writtenFrames)
            assertTrue(events.isEmpty())
        }

    @Test
    fun `GIVEN a run superseded by a new play WHEN the new play ends THEN only the new playback ends`() = runTest {
        // Given
        prepareScenario(writableBytes = 0)
        play(playbackId = 1)
        runCurrent()

        // When
        play(playbackId = 2)
        output.writableBytes = Int.MAX_VALUE
        runCurrent()
        output.playedFrames = output.writtenFrames
        advanceTimeBy(3.seconds)

        // Then
        assertEquals(TONE_FRAMES, output.writtenFrames)
        assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(2)), events)
    }

    @Test
    fun `GIVEN a full output not playing WHEN the stall timeout passes THEN emits Failed not blaming the file`() =
        runTest {
            // Given
            prepareScenario(writableBytes = 0)
            play(playbackId = 8)
            runCurrent()

            // When
            advanceTimeBy(2500.milliseconds)

            // Then
            assertEquals(
                listOf<ChapterAudioPlayerEvent>(
                    ChapterAudioPlayerEvent.Failed(
                        playbackId = 8,
                        isFileUnreadable = false,
                    ),
                ),
                events,
            )
        }

    @Test
    fun `GIVEN a full output not playing WHEN the stall timeout passes THEN closes the output`() = runTest {
        // Given
        prepareScenario(writableBytes = 0)
        play()
        runCurrent()

        // When
        advanceTimeBy(2500.milliseconds)

        // Then
        assertEquals(listOf(OutputCall.START, OutputCall.STOP_AND_DISCARD, OutputCall.CLOSE), output.calls)
    }

    @Test
    fun `GIVEN an output that stalled while writing WHEN playing again THEN opens a new output`() = runTest {
        // Given
        prepareScenario(writableBytes = 0)
        play(playbackId = 1)
        runCurrent()
        advanceTimeBy(2500.milliseconds)

        // When
        play(playbackId = 2)
        runCurrent()

        // Then
        assertEquals(2, outputs.size)
        assertEquals(listOf(OutputCall.START), outputs.last().calls)
    }

    @Test
    fun `GIVEN a full output not playing WHEN under the stall timeout THEN emits nothing`() = runTest {
        // Given
        prepareScenario(writableBytes = 0)
        play()
        runCurrent()

        // When
        advanceTimeBy(1900.milliseconds)

        // Then
        assertTrue(events.isEmpty())
    }

    @Test
    fun `GIVEN a full output WHEN its played frames keep advancing past the stall timeout THEN emits nothing`() =
        runTest {
            // Given
            prepareScenario(writableBytes = 0)
            play()
            runCurrent()

            // When
            repeat(50) {
                output.playedFrames += 100
                advanceTimeBy(100.milliseconds)
            }

            // Then
            assertTrue(events.isEmpty())
            assertEquals(listOf(OutputCall.START), output.calls)
        }

    @Test
    fun `GIVEN played frames that stop WHEN room keeps freeing up past the stall timeout THEN emits nothing`() =
        runTest {
            // Given
            prepareScenario(writableBytes = 0)
            play()
            runCurrent()

            // When
            repeat(10) {
                output.writableBytes = SMALL_ROOM_BYTES
                advanceTimeBy(500.milliseconds)
            }

            // Then
            assertTrue(events.isEmpty())
            assertEquals(10L * SMALL_ROOM_BYTES / FRAME_SIZE, output.writtenFrames)
        }

    @Test
    fun `GIVEN every frame written WHEN the output stops beyond the end slack for the stall timeout THEN fails`() =
        runTest {
            // Given
            prepareScenario()
            play(playbackId = 3)
            runCurrent()

            // When
            output.playedFrames = output.writtenFrames - END_SLACK_FRAMES - 1
            advanceTimeBy(2500.milliseconds)

            // Then
            assertEquals(
                listOf<ChapterAudioPlayerEvent>(
                    ChapterAudioPlayerEvent.Failed(
                        playbackId = 3,
                        isFileUnreadable = false,
                    ),
                ),
                events,
            )
        }

    @Test
    fun `GIVEN every frame written WHEN the output stops beyond the end slack for the stall timeout THEN closes it`() =
        runTest {
            // Given
            prepareScenario()
            play()
            runCurrent()

            // When
            output.playedFrames = output.writtenFrames - END_SLACK_FRAMES - 1
            advanceTimeBy(2500.milliseconds)

            // Then
            assertEquals(listOf(OutputCall.START, OutputCall.STOP_AND_DISCARD, OutputCall.CLOSE), output.calls)
        }

    @Test
    fun `GIVEN every frame written WHEN the output stops at the end slack for the stall timeout THEN emits Ended`() =
        runTest {
            // Given
            prepareScenario()
            play(playbackId = 3)
            runCurrent()

            // When
            output.playedFrames = output.writtenFrames - END_SLACK_FRAMES
            advanceTimeBy(2500.milliseconds)

            // Then
            assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(3)), events)
        }

    @Test
    fun `GIVEN a pause while the output opens WHEN the run continues THEN closes it without starting it`() = runTest {
        // Given
        prepareScenario(whileOpeningFirstOutput = { player.pause() })
        play()

        // When
        runCurrent()

        // Then
        assertEquals(listOf(OutputCall.CLOSE), outputs.single().calls)
        assertEquals(0, outputs.single().writtenFrames)
    }

    @Test
    fun `GIVEN a pause while the output opens WHEN time passes THEN emits nothing`() = runTest {
        // Given
        prepareScenario(whileOpeningFirstOutput = { player.pause() })
        play()

        // When
        advanceTimeBy(3.seconds)

        // Then
        assertTrue(events.isEmpty())
    }

    @Test
    fun `GIVEN a seek while the output opens WHEN the run continues THEN plays the new position on a new output`() =
        runTest {
            // Given
            prepareScenario(whileOpeningFirstOutput = { player.seekTo(1.seconds) })
            play()

            // When
            runCurrent()

            // Then
            assertEquals(listOf(OutputCall.CLOSE), outputs.first().calls)
            assertEquals(0, outputs.first().writtenFrames)
            assertEquals(TONE_FRAMES - FRAME_RATE, outputs.last().writtenFrames)
            assertEquals(1.seconds, player.position)
        }

    @Test
    fun `GIVEN a new play while the output opens WHEN the new play ends THEN only the new playback ends`() = runTest {
        // Given
        prepareScenario(whileOpeningFirstOutput = { play(playbackId = 2) })
        play(playbackId = 1)
        runCurrent()

        // When
        output.playedFrames = output.writtenFrames
        advanceTimeBy(3.seconds)

        // Then
        assertEquals(listOf(OutputCall.CLOSE), outputs.first().calls)
        assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(2)), events)
    }

    @Test
    fun `GIVEN a release while the output opens WHEN time passes THEN closes it and emits nothing`() = runTest {
        // Given
        prepareScenario(whileOpeningFirstOutput = { player.release() })
        play()

        // When
        advanceTimeBy(3.seconds)

        // Then
        assertEquals(listOf(OutputCall.CLOSE), outputs.single().calls)
        assertTrue(events.isEmpty())
        assertEquals(Duration.ZERO, player.position)
    }

    @Test
    fun `GIVEN an output in another format WHEN playing again THEN closes the old output before opening a new one`() =
        runTest {
            // Given
            prepareScenario(firstOutputFormat = otherFormat)
            play(playbackId = 1)
            runCurrent()

            // When
            play(playbackId = 2)
            runCurrent()

            // Then
            assertEquals(2, outputs.size)
            assertEquals(
                listOf(OutputCall.START, OutputCall.STOP_AND_DISCARD, OutputCall.CLOSE),
                previousOutputCallsAtLastOpen,
            )
        }

    private fun play(
        path: String = toneFile.path,
        position: Duration = Duration.ZERO,
        speed: Float = 1f,
        playbackId: Int = 1,
    ) {
        player.play(
            audioPath = path,
            position = position,
            speed = speed,
            playbackId = playbackId,
        )
    }

    private fun TestScope.prepareScenario(
        failingOpens: Int = 0,
        writableBytes: Int = Int.MAX_VALUE,
        firstOutputFormat: AudioFormat? = null,
        whileOpeningFirstOutput: () -> Unit = {},
    ) {
        var remainingFailures = failingOpens
        outputs = mutableListOf()
        events = mutableListOf()
        previousOutputCallsAtLastOpen = null
        player = DesktopChapterAudioPlayer(
            openOutput = { format ->
                if (remainingFailures > 0) {
                    remainingFailures--
                    throw LineUnavailableException("The audio device is busy")
                }
                previousOutputCallsAtLastOpen = outputs.lastOrNull()?.calls?.toList()
                val isFirstOutput = outputs.isEmpty()
                val opened = FakePcmOutput(
                    format = firstOutputFormat?.takeIf { isFirstOutput } ?: format,
                    writableBytes = writableBytes,
                ).also(outputs::add)
                if (isFirstOutput) whileOpeningFirstOutput()
                opened
            },
            playbackDispatcher = StandardTestDispatcher(testScheduler),
        )
        backgroundScope.launch { player.events.collect { event -> events += event } }
    }

    private companion object {
        const val FRAME_RATE = 24_000
        const val TONE_FRAMES = 49_536L
        const val LENGTH_TOLERANCE = 0.03
        const val FRAME_SIZE = 2
        const val END_SLACK_FRAMES = 2_400L
        const val SMALL_ROOM_BYTES = 200
    }
}

private class FakePcmOutput(
    override val format: AudioFormat,
    override var writableBytes: Int,
) : PcmOutput {
    override var playedFrames: Long = 0
    var writtenFrames: Long = 0
    val calls = mutableListOf<OutputCall>()

    override fun write(
        bytes: ByteArray,
        offset: Int,
        length: Int,
    ) {
        writtenFrames += length / format.frameSize
        writableBytes -= length
    }

    override fun start() {
        calls += OutputCall.START
    }

    override fun stopAndDiscard() {
        calls += OutputCall.STOP_AND_DISCARD
    }

    override fun close() {
        calls += OutputCall.CLOSE
    }
}
