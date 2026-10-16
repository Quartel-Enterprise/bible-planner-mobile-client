package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.data.exception.ChapterAudioDecodeException
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.decoder.SampleBuffer
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import javax.sound.sampled.AudioFormat
import kotlin.time.Duration

/*
 * Why: seeking decodes nothing before the start, it only walks the frame headers, so jumping to the
 * end of a long psalm is as fast as starting a chapter; the few frames right before the start are
 * decoded and dropped because a frame's audio can borrow bytes from the frames before it.
 */
internal class Mp3PcmSource(
    path: String,
    start: Duration,
) : AutoCloseable {
    private val input: FileInputStream = wrapDecodeErrors { FileInputStream(path) }
    private val bitstream: Bitstream = closeOnFailure { Bitstream(BufferedInputStream(input)) }
    private val decoder = Decoder()
    private var pendingHeader: Header? = closeOnFailure { bitstream.readFrame() }
    private val samplesPerFrame: Int
    private val startFrame: Long
    private var leadingSamplesToDrop: Int
    private var frameIndex = 0L

    val format: AudioFormat

    init {
        val firstHeader = pendingHeader ?: run {
            close()
            throw ChapterAudioDecodeException(cause = null)
        }
        val sampleRate = firstHeader.frequency()
        format = AudioFormat(
            sampleRate.toFloat(),
            BITS_PER_SAMPLE,
            if (firstHeader.mode() == Header.SINGLE_CHANNEL) 1 else 2,
            true,
            false,
        )
        samplesPerFrame = firstHeader.countSamplesPerFrame()
        val startSample = start.inWholeMicroseconds * sampleRate / MICROSECONDS_PER_SECOND
        startFrame = startSample / samplesPerFrame
        leadingSamplesToDrop = (startSample % samplesPerFrame).toInt()
    }

    fun read(): ShortArray? {
        while (true) {
            val header = readHeader() ?: return null
            val index = frameIndex++
            if (index < startFrame - WARM_UP_FRAMES) {
                bitstream.closeFrame()
                continue
            }
            val samples = decode(header)
            if (index >= startFrame) return dropLeadingSamples(samples)
        }
    }

    override fun close() {
        wrapDecodeErrors { bitstream.close() }
    }

    private fun readHeader(): Header? {
        val header = pendingHeader ?: return wrapDecodeErrors { bitstream.readFrame() }
        pendingHeader = null
        return header
    }

    // Why: a frame whose borrowed bytes were skipped decodes to nothing, and is padded so the timeline holds.
    private fun decode(header: Header): ShortArray {
        val buffer = wrapDecodeErrors { decoder.decodeFrame(header, bitstream) as SampleBuffer }
        bitstream.closeFrame()
        val samples = ShortArray(samplesPerFrame * format.channels)
        buffer.buffer.copyInto(
            destination = samples,
            endIndex = minOf(buffer.bufferLength, samples.size),
        )
        return samples
    }

    private fun dropLeadingSamples(samples: ShortArray): ShortArray {
        if (leadingSamplesToDrop == 0) return samples
        val trimmed = samples.copyOfRange(leadingSamplesToDrop * format.channels, samples.size)
        leadingSamplesToDrop = 0
        return trimmed
    }

    private fun Header.countSamplesPerFrame(): Int = when {
        layer() == LAYER_I -> SAMPLES_LAYER_I
        layer() == LAYER_III && version() != Header.MPEG1 -> SAMPLES_LAYER_III_LOW_SAMPLE_RATE
        else -> SAMPLES_DEFAULT
    }

    // Why: a constructor that throws never reaches close(), and an open file can't be evicted on Windows.
    private inline fun <T> closeOnFailure(block: () -> T): T = try {
        wrapDecodeErrors(block)
    } catch (exception: ChapterAudioDecodeException) {
        input.close()
        throw exception
    }

    // Why: JLayer reports a corrupt frame with an out-of-bounds index as often as with its own exceptions.
    private inline fun <T> wrapDecodeErrors(block: () -> T): T = try {
        block()
    } catch (exception: JavaLayerException) {
        throw ChapterAudioDecodeException(exception)
    } catch (exception: IOException) {
        throw ChapterAudioDecodeException(exception)
    } catch (exception: IndexOutOfBoundsException) {
        throw ChapterAudioDecodeException(exception)
    }

    private companion object {
        const val BITS_PER_SAMPLE = 16
        const val MICROSECONDS_PER_SECOND = 1_000_000L
        const val WARM_UP_FRAMES = 4
        const val LAYER_I = 1
        const val LAYER_III = 3
        const val SAMPLES_LAYER_I = 384
        const val SAMPLES_LAYER_III_LOW_SAMPLE_RATE = 576
        const val SAMPLES_DEFAULT = 1152
    }
}
