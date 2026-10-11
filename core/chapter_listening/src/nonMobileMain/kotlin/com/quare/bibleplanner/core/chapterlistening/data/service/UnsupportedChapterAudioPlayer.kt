package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration

internal class UnsupportedChapterAudioPlayer : ChapterAudioPlayer {
    override val isSupported: Boolean = false
    override val events: Flow<ChapterAudioPlayerEvent> = emptyFlow()
    override val position: Duration = Duration.ZERO

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) {
    }

    override fun setSpeed(speed: Float) {
    }

    override fun pause() {
    }

    override fun release() {
    }
}
