package com.quare.bibleplanner.core.chapterlistening.data.service

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.SourceDataLine

internal class JavaSoundPcmOutput(
    private val line: SourceDataLine,
) : PcmOutput {
    override val format: AudioFormat
        get() = line.format

    override val playedFrames: Long
        get() = line.longFramePosition

    override val writableBytes: Int
        get() = line.available()

    override fun write(
        bytes: ByteArray,
        offset: Int,
        length: Int,
    ) {
        line.write(bytes, offset, length)
    }

    override fun start() {
        line.start()
    }

    override fun stopAndDiscard() {
        line.stop()
        line.flush()
    }

    override fun close() {
        line.close()
    }
}
