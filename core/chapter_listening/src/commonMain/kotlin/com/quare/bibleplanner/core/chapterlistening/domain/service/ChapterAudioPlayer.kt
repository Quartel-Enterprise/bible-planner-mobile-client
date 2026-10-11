package com.quare.bibleplanner.core.chapterlistening.domain.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface ChapterAudioPlayer {
    val isSupported: Boolean
    val events: Flow<ChapterAudioPlayerEvent>
    val position: Duration

    fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    )

    fun setSpeed(speed: Float)

    fun pause()

    fun release()
}
