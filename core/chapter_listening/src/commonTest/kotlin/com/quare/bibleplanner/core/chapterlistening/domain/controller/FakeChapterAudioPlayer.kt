package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Duration

internal class FakeChapterAudioPlayer : ChapterAudioPlayer {
    override val isSupported: Boolean = true
    private val eventFlow = MutableSharedFlow<ChapterAudioPlayerEvent>(extraBufferCapacity = 16)
    override val events: Flow<ChapterAudioPlayerEvent> = eventFlow
    override var position: Duration = Duration.ZERO
    val plays = mutableListOf<PlayCall>()
    var speed: Float? = null
    var releaseCount = 0
    var pauseCount = 0
    val seeks = mutableListOf<Duration>()

    fun emit(event: ChapterAudioPlayerEvent) {
        eventFlow.tryEmit(event)
    }

    fun finish() {
        emit(ChapterAudioPlayerEvent.Ended(plays.last().playbackId))
    }

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) {
        plays += PlayCall(
            audioPath = audioPath,
            position = position,
            playbackId = playbackId,
        )
        this.position = position
        this.speed = speed
    }

    override fun seekTo(position: Duration) {
        seeks += position
    }

    override fun setSpeed(speed: Float) {
        this.speed = speed
    }

    override fun pause() {
        pauseCount++
    }

    override fun release() {
        releaseCount++
    }
}
