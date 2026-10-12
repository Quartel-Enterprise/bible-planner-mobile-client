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
    createPlayer: () -> ExoPlayer,
) : ChapterAudioPlayer {
    override val isSupported: Boolean = true

    private val eventChannel = Channel<ChapterAudioPlayerEvent>(Channel.UNLIMITED)
    override val events: Flow<ChapterAudioPlayerEvent> = eventChannel.receiveAsFlow()

    // Why: only these mean the cached file itself is broken; a decoder or audio output failure must not delete it.
    private val unreadableFileErrorCodes = setOf(
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
    )
    private var loadedPath: String? = null

    // Why: a failed player keeps its position for the retry, but must load the file again before it plays.
    private var hasFailed = false
    private var playbackId = 0

    // Why: created on the first play, so checking support or opening the reader never builds an ExoPlayer.
    private val player: ExoPlayer by lazy { createPlayer().also { it.addListener(PlaybackListener()) } }

    override val position: Duration
        get() = if (loadedPath == null) Duration.ZERO else player.currentPosition.milliseconds

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) {
        this.playbackId = playbackId
        if (loadedPath == audioPath && !hasFailed) {
            player.seekTo(position.inWholeMilliseconds)
        } else {
            player.setMediaItem(
                MediaItem.fromUri(Uri.fromFile(File(audioPath))),
                position.inWholeMilliseconds,
            )
            player.prepare()
            loadedPath = audioPath
            hasFailed = false
        }
        player.setPlaybackSpeed(speed)
        player.play()
    }

    override fun seekTo(position: Duration) {
        if (loadedPath != null) player.seekTo(position.inWholeMilliseconds)
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
        hasFailed = false
    }

    private inner class PlaybackListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) eventChannel.trySend(ChapterAudioPlayerEvent.Ended(playbackId))
        }

        override fun onPlayerError(error: PlaybackException) {
            Logger.e(error) { "Could not play the chapter audio" }
            hasFailed = true
            eventChannel.trySend(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = playbackId,
                    isFileUnreadable = error.errorCode in unreadableFileErrorCodes,
                ),
            )
        }
    }
}
