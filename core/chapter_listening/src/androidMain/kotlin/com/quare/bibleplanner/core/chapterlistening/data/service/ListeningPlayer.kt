package com.quare.bibleplanner.core.chapterlistening.data.service

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import kotlin.time.Duration.Companion.milliseconds

/*
 * Why: the session shows the ExoPlayer that plays the chapter, so the notification, the lock screen
 * and the headset get its real position and can scrub it; every command they send goes to the
 * controller instead of the ExoPlayer, which keeps a single owner of the playback. Next and previous
 * skip a verse, and a chapter that is still loading reads as buffering so Media3 keeps the service in
 * the foreground.
 */
@OptIn(UnstableApi::class)
internal class ListeningPlayer(
    private val events: ListeningPlayerEvents,
    exoPlayer: ExoPlayer,
) : ForwardingSimpleBasePlayer(exoPlayer) {
    private val availableCommands = Player.Commands
        .Builder()
        .addAll(
            COMMAND_PLAY_PAUSE,
            COMMAND_STOP,
            COMMAND_SEEK_TO_NEXT,
            COMMAND_SEEK_TO_PREVIOUS,
            COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
            COMMAND_GET_CURRENT_MEDIA_ITEM,
            COMMAND_GET_TIMELINE,
            COMMAND_GET_METADATA,
        ).build()
    private var nowPlaying: NowPlayingModel? = null

    init {
        exoPlayer.addListener(InterruptionListener(exoPlayer))
    }

    fun show(nowPlaying: NowPlayingModel?) {
        this.nowPlaying = nowPlaying
        invalidateState()
    }

    fun send(command: ListeningRemoteCommand) {
        events.send(command)
    }

    override fun getState(): State {
        val current = nowPlaying ?: return State
            .Builder()
            .setAvailableCommands(availableCommands)
            .setPlaybackState(STATE_IDLE)
            .build()
        val forwarded = super.getState()
        // Why: until a chapter's audio loads, the ExoPlayer still holds the previous chapter's file.
        val isLoading = !current.isAudioLoaded || (current.isPlaying && !forwarded.playWhenReady)
        val loadedDurationUs = forwarded.playlist.firstOrNull()?.durationUs ?: C.TIME_UNSET
        val item = MediaItemData
            .Builder(current.chapterTitle)
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(
                        current.verseNumber?.let { number ->
                            "${current.chapterTitle}:$number"
                        } ?: current.chapterTitle,
                    ).setArtist(current.subtitle)
                    .setAlbumTitle(current.chapterTitle)
                    .build(),
                // Why: until the file loads its length is unknown, rather than a segment length in other units.
            ).setDurationUs(if (isLoading) C.TIME_UNSET else loadedDurationUs)
            .setIsSeekable(!isLoading)
            .build()
        return forwarded
            .buildUpon()
            .setAvailableCommands(availableCommands)
            // Why: the controller reports a failed file itself, and Media3 only accepts an error while idle.
            .setPlayerError(null)
            .setPlaylist(listOf(item))
            .setCurrentMediaItemIndex(0)
            .setContentPositionMs(
                if (isLoading) PositionSupplier.getConstant(0) else forwarded.contentPositionMsSupplier,
            ).setPlayWhenReady(current.isPlaying, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setPlaybackState(
                when {
                    isLoading && current.isPlaying -> STATE_BUFFERING
                    current.isPlaying && forwarded.playbackState == STATE_BUFFERING -> STATE_BUFFERING
                    else -> STATE_READY
                },
            ).build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        send(if (playWhenReady) ListeningRemoteCommand.Play else ListeningRemoteCommand.Pause)
        return Futures.immediateVoidFuture()
    }

    override fun handlePrepare(): ListenableFuture<*> = Futures.immediateVoidFuture()

    override fun handleSeek(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int,
    ): ListenableFuture<*> {
        val command = when (seekCommand) {
            COMMAND_SEEK_TO_NEXT, COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> ListeningRemoteCommand.NextVerse
            COMMAND_SEEK_TO_PREVIOUS, COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> ListeningRemoteCommand.PreviousVerse
            else -> ListeningRemoteCommand.SeekTo(positionMs.coerceAtLeast(0).milliseconds)
        }
        send(command)
        return Futures.immediateVoidFuture()
    }

    override fun handleStop(): ListenableFuture<*> {
        send(ListeningRemoteCommand.Stop)
        return Futures.immediateVoidFuture()
    }

    // Why: ExoPlayer pauses itself on focus loss; the controller must hear about it.
    private inner class InterruptionListener(
        private val exoPlayer: ExoPlayer,
    ) : Player.Listener {
        private var isTransientlySuppressed = false

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: Int,
        ) {
            if (!playWhenReady && reason == PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) {
                events.interrupt(AudioInterruptionModel.PAUSE_REQUESTED)
            }
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            val isTransient = playbackSuppressionReason == PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
            if (isTransient == isTransientlySuppressed) return
            isTransientlySuppressed = isTransient
            // Why: a focus lost for good also lifts the suppression, but paused; that is a pause, not the call ending.
            if (!isTransient && !exoPlayer.playWhenReady) return
            events.interrupt(if (isTransient) AudioInterruptionModel.BEGAN else AudioInterruptionModel.ENDED)
        }
    }
}
