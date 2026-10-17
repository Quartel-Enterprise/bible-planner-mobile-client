package com.quare.bibleplanner.core.chapterlistening.data.service

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDecodeException
import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioOutputStalledException
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.sound.sampled.AudioFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/*
 * Why: every start, seek and speed change runs a fresh decode from the requested position on a
 * background coroutine, and the run id taken under the lock drops whatever an older run was still
 * about to write; the lock is never held while the line opens or waits for room, so the main thread
 * never waits for the audio device.
 */
internal class DesktopChapterAudioPlayer(
    private val openOutput: (AudioFormat) -> PcmOutput,
    playbackDispatcher: CoroutineDispatcher,
) : ChapterAudioPlayer {
    override val isSupported: Boolean = true

    private val eventChannel = Channel<ChapterAudioPlayerEvent>(Channel.UNLIMITED)
    override val events: Flow<ChapterAudioPlayerEvent> = eventChannel.receiveAsFlow()

    private val scope = CoroutineScope(SupervisorJob() + playbackDispatcher)
    private val lock = Any()
    private val writePollInterval: Duration = 10.milliseconds

    // Why: a device that was unplugged or taken away stops consuming frames without reporting any error.
    private val outputStallTimeout: Duration = 2.seconds

    // Why: some audio drivers stop a few frames short of the last one, which is still a finished chapter.
    private val endSlack: Duration = 100.milliseconds

    private var output: PcmOutput? = null
    private var loadedPath: String? = null
    private var runId = 0
    private var runJob: Job? = null
    private var isRunning = false
    private var playbackId = 0
    private var speed = 1f
    private var pausedPosition = Duration.ZERO
    private var runStart = Duration.ZERO
    private var runStartFrame: Long? = null

    override val position: Duration
        get() = synchronized(lock) { readPosition() }

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) {
        synchronized(lock) {
            this.playbackId = playbackId
            this.speed = speed
            loadedPath = audioPath
            startRun(position)
        }
    }

    override fun seekTo(position: Duration) {
        synchronized(lock) {
            if (loadedPath == null) return
            if (isRunning) startRun(position) else pausedPosition = position
        }
    }

    override fun setSpeed(speed: Float) {
        synchronized(lock) {
            if (speed == this.speed) return
            val position = readPosition()
            this.speed = speed
            if (isRunning) startRun(position)
        }
    }

    override fun pause() {
        synchronized(lock) {
            if (!isRunning) return
            pausedPosition = readPosition()
            stopRun()
        }
    }

    override fun release() {
        synchronized(lock) {
            stopRun()
            output?.close()
            output = null
            loadedPath = null
            pausedPosition = Duration.ZERO
        }
    }

    private fun startRun(position: Duration) {
        stopRun()
        val path = loadedPath ?: return
        val id = runId
        isRunning = true
        runStart = position
        val runSpeed = speed
        val runPlaybackId = playbackId
        runJob = scope.launch {
            suspendRunCatching {
                runPlayback(
                    id = id,
                    path = path,
                    start = position,
                    speed = runSpeed,
                )
            }.onSuccess { isComplete ->
                if (isComplete) {
                    finishRun(
                        id = id,
                        playbackId = runPlaybackId,
                    )
                }
            }.onFailure { throwable ->
                failRun(
                    id = id,
                    playbackId = runPlaybackId,
                    throwable = throwable,
                )
            }
        }
    }

    private fun stopRun() {
        runId++
        runJob?.cancel()
        runJob = null
        isRunning = false
        runStartFrame = null
        output?.stopAndDiscard()
    }

    // Why: the frames the line has played, scaled by the speed, are how far the voice got in the file.
    private fun readPosition(): Duration {
        if (!isRunning) return pausedPosition
        val startFrame = runStartFrame ?: return runStart
        val activeOutput = output ?: return runStart
        val playedFrames = (activeOutput.playedFrames - startFrame).coerceAtLeast(0)
        return runStart + (playedFrames * speed / activeOutput.format.frameRate).toDouble().seconds
    }

    private suspend fun runPlayback(
        id: Int,
        path: String,
        start: Duration,
        speed: Float,
    ): Boolean = Mp3PcmSource(
        path = path,
        start = start,
    ).use { source ->
        val activeOutput = prepareOutput(
            id = id,
            format = source.format,
        ) ?: return false
        val stretcher = SpeechTimeStretcher(
            sampleRate = source.format.sampleRate.toInt(),
            channels = source.format.channels,
            speed = speed,
        )
        val writer = PcmWriter(
            id = id,
            output = activeOutput,
        )
        while (true) {
            val samples = source.read() ?: break
            if (!writer.write(stretcher.transform(samples))) return false
        }
        if (!writer.write(stretcher.takeRemaining())) return false
        awaitPlayed(
            id = id,
            output = activeOutput,
            frames = writer.writtenFrames,
        )
    }

    private fun prepareOutput(
        id: Int,
        format: AudioFormat,
    ): PcmOutput? {
        val stale = synchronized(lock) {
            if (id != runId) return null
            val current = output
            if (current != null && current.format.matches(format)) return startOutput(current)
            output = null
            current
        }
        stale?.close()
        val opened = openOutput(format)
        return synchronized(lock) {
            if (id == runId) {
                output = opened
                startOutput(opened)
            } else {
                null
            }
        } ?: run {
            opened.close()
            null
        }
    }

    private fun startOutput(output: PcmOutput): PcmOutput {
        output.start()
        runStartFrame = output.playedFrames
        return output
    }

    private fun readPlayedFrames(id: Int): Long? = synchronized(lock) {
        if (id != runId) return null
        val startFrame = runStartFrame ?: return null
        output?.playedFrames?.minus(startFrame)
    }

    private suspend fun awaitPlayed(
        id: Int,
        output: PcmOutput,
        frames: Long,
    ): Boolean {
        val slackFrames = (output.format.frameRate * endSlack.inWholeMilliseconds / MILLISECONDS_PER_SECOND).toLong()
        val stallWatch = StallWatch()
        while (true) {
            val played = readPlayedFrames(id) ?: return false
            if (played >= frames) return true
            if (stallWatch.hasStalled(played)) {
                if (frames - played <= slackFrames) return true
                throw ChapterAudioOutputStalledException()
            }
            delay(writePollInterval)
        }
    }

    private fun finishRun(
        id: Int,
        playbackId: Int,
    ) {
        synchronized(lock) {
            if (id != runId) return
            pausedPosition = readPosition()
            stopRun()
        }
        eventChannel.trySend(ChapterAudioPlayerEvent.Ended(playbackId))
    }

    private fun failRun(
        id: Int,
        playbackId: Int,
        throwable: Throwable,
    ) {
        synchronized(lock) {
            if (id != runId) return
            pausedPosition = runStart
            stopRun()
            // Why: a line that failed may be left half-open, so the next start opens a new one.
            if (throwable !is ChapterAudioDecodeException) {
                output?.close()
                output = null
            }
        }
        Logger.e(throwable) { "Could not play the chapter audio" }
        eventChannel.trySend(
            ChapterAudioPlayerEvent.Failed(
                playbackId = playbackId,
                isFileUnreadable = throwable is ChapterAudioDecodeException,
            ),
        )
    }

    // Why: it only writes what fits in the line, so a stopped run never blocks inside the audio driver.
    private inner class PcmWriter(
        private val id: Int,
        private val output: PcmOutput,
    ) {
        private val frameSize = output.format.frameSize
        private var bytes = ByteArray(0)

        var writtenFrames = 0L
            private set

        suspend fun write(samples: ShortArray): Boolean {
            val length = encode(samples)
            val stallWatch = StallWatch()
            var offset = 0
            while (offset < length) {
                val written = writeAvailable(
                    offset = offset,
                    length = length,
                ) ?: return false
                offset += written
                if (written > 0) {
                    stallWatch.reset()
                    continue
                }
                if (stallWatch.hasStalled(readPlayedFrames(id) ?: return false)) {
                    throw ChapterAudioOutputStalledException()
                }
                delay(writePollInterval)
            }
            writtenFrames += length / frameSize
            return true
        }

        private fun writeAvailable(
            offset: Int,
            length: Int,
        ): Int? = synchronized(lock) {
            if (id != runId) return null
            val chunk = minOf(output.writableBytes, length - offset) / frameSize * frameSize
            if (chunk > 0) {
                output.write(
                    bytes = bytes,
                    offset = offset,
                    length = chunk,
                )
            }
            chunk
        }

        private fun encode(samples: ShortArray): Int {
            val length = samples.size * Short.SIZE_BYTES
            if (bytes.size < length) bytes = ByteArray(length)
            samples.forEachIndexed { index, sample ->
                val value = sample.toInt()
                bytes[index * 2] = value.toByte()
                bytes[index * 2 + 1] = (value shr Byte.SIZE_BITS).toByte()
            }
            return length
        }
    }

    // Why: a position that stops moving for the whole timeout is a stalled device, not a slow one.
    private inner class StallWatch {
        private var lastPlayed = -1L
        private var stalledFor = Duration.ZERO

        fun hasStalled(played: Long): Boolean {
            stalledFor = if (played == lastPlayed) stalledFor + writePollInterval else Duration.ZERO
            lastPlayed = played
            return stalledFor >= outputStallTimeout
        }

        fun reset() {
            lastPlayed = -1L
            stalledFor = Duration.ZERO
        }
    }

    private companion object {
        const val MILLISECONDS_PER_SECOND = 1000
    }
}
