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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.Duration
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
    exoPlayer: ExoPlayer,
) : ForwardingSimpleBasePlayer(exoPlayer) {
    private val commandChannel = Channel<ListeningRemoteCommand>(Channel.UNLIMITED)
    val commands: Flow<ListeningRemoteCommand> = commandChannel.receiveAsFlow()

    private val interruptionChannel = Channel<AudioInterruptionModel>(Channel.UNLIMITED)
    val interruptions: Flow<AudioInterruptionModel> = interruptionChannel.receiveAsFlow()

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
        commandChannel.trySend(command)
    }

    override fun getState(): State {
        val current = nowPlaying ?: return State
            .Builder()
            .setAvailableCommands(availableCommands)
            .setPlaybackState(STATE_IDLE)
            .build()
        val forwarded = super.getState()
        // Why: while the next chapter loads the ExoPlayer still holds the previous one, paused.
        val isLoading = current.isPlaying && !forwarded.playWhenReady
        val loadedDurationUs = forwarded.playlist.firstOrNull()?.durationUs ?: C.TIME_UNSET
        val item = MediaItemData
            .Builder(current.chapterTitle)
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(current.chapterTitle)
                    .setArtist(current.subtitle)
                    .build(),
            ).setDurationUs(
                if (isLoading ||
                    loadedDurationUs == C.TIME_UNSET
                ) {
                    toDurationUs(current)
                } else {
                    loadedDurationUs
                },
            ).setIsSeekable(true)
            .build()
        return forwarded
            .buildUpon()
            .setAvailableCommands(availableCommands)
            .setPlaylist(listOf(item))
            .setCurrentMediaItemIndex(0)
            .setContentPositionMs(
                if (isLoading) PositionSupplier.getConstant(0) else forwarded.contentPositionMsSupplier,
            ).setPlayWhenReady(current.isPlaying, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setPlaybackState(
                when {
                    isLoading -> STATE_BUFFERING
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

    private fun toDurationUs(nowPlaying: NowPlayingModel): Long = nowPlaying.verses
        .fold(Duration.ZERO) { total, verse -> total + verse.duration }
        .inWholeMicroseconds

    // Why: ExoPlayer pauses itself on focus loss and unplugged headphones; the controller must hear about it.
    private inner class InterruptionListener(
        private val exoPlayer: ExoPlayer,
    ) : Player.Listener {
        private var isTransientlySuppressed = false

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: Int,
        ) {
            val isSystemPause = reason == PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS ||
                reason == PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
            if (!playWhenReady && isSystemPause) interruptionChannel.trySend(AudioInterruptionModel.PAUSE_REQUESTED)
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            val isTransient = playbackSuppressionReason == PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
            if (isTransient == isTransientlySuppressed) return
            isTransientlySuppressed = isTransient
            // Why: a focus lost for good also lifts the suppression, but paused; that is a pause, not the call ending.
            if (!isTransient && !exoPlayer.playWhenReady) return
            interruptionChannel.trySend(
                if (isTransient) AudioInterruptionModel.BEGAN else AudioInterruptionModel.ENDED,
            )
        }
    }
}
