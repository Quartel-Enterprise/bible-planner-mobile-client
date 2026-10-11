package com.quare.bibleplanner.core.chapterlistening.data.service

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class AndroidChapterAudioPlayer(
    private val player: ExoPlayer,
) : ChapterAudioPlayer {
    override val isSupported: Boolean = true

    private val eventChannel = Channel<ChapterAudioPlayerEvent>(Channel.UNLIMITED)
    override val events: Flow<ChapterAudioPlayerEvent> = eventChannel.receiveAsFlow()

    private var loadedPath: String? = null
    private var playbackId = 0

    init {
        player.addListener(PlaybackListener())
    }

    override val position: Duration
        get() = if (loadedPath == null) Duration.ZERO else player.currentPosition.milliseconds

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) {
        this.playbackId = playbackId
        if (loadedPath == audioPath) {
            player.seekTo(position.inWholeMilliseconds)
        } else {
            player.setMediaItem(
                MediaItem.fromUri(Uri.fromFile(File(audioPath))),
                position.inWholeMilliseconds,
            )
            player.prepare()
            loadedPath = audioPath
        }
        player.setPlaybackSpeed(speed)
        player.play()
    }

    override fun setSpeed(speed: Float) {
        if (loadedPath != null) player.setPlaybackSpeed(speed)
    }

    override fun pause() {
        if (loadedPath != null) player.pause()
    }

    override fun release() {
        if (loadedPath == null) return
        player.stop()
        player.clearMediaItems()
        loadedPath = null
    }

    private inner class PlaybackListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) eventChannel.trySend(ChapterAudioPlayerEvent.Ended(playbackId))
        }

        override fun onPlayerError(error: PlaybackException) {
            Logger.e(error) { "Could not play the chapter audio" }
            loadedPath = null
            eventChannel.trySend(ChapterAudioPlayerEvent.Failed(playbackId))
        }
    }
}
