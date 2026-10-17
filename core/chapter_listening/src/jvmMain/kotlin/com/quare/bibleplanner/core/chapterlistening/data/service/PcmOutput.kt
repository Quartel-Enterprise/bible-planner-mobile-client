package com.quare.bibleplanner.core.chapterlistening.data.service

import javax.sound.sampled.AudioFormat

internal interface PcmOutput {
    val format: AudioFormat
    val playedFrames: Long
    val writableBytes: Int

    fun write(
        bytes: ByteArray,
        offset: Int,
        length: Int,
    )

    fun start()

    fun stopAndDiscard()

    fun close()
}
