package com.quare.bibleplanner.core.chapterlistening.data.service

import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/*
 * Why: each verse is one media item, so the notification, the lock screen and the headset buttons
 * skip verse by verse through Media3's own next/previous handling, which a single item would ignore.
 */
@OptIn(UnstableApi::class)
internal class ListeningPlayer : SimpleBasePlayer(Looper.getMainLooper()) {
    private val commandChannel = Channel<ListeningRemoteCommand>(Channel.UNLIMITED)
    val commands: Flow<ListeningRemoteCommand> = commandChannel.receiveAsFlow()

    private val availableCommands = Player.Commands
        .Builder()
        .addAll(
            COMMAND_PLAY_PAUSE,
            COMMAND_STOP,
            COMMAND_SEEK_TO_NEXT,
            COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            COMMAND_SEEK_TO_PREVIOUS,
            COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            COMMAND_SEEK_TO_MEDIA_ITEM,
            COMMAND_GET_CURRENT_MEDIA_ITEM,
            COMMAND_GET_TIMELINE,
            COMMAND_GET_METADATA,
        ).build()
    private var nowPlaying: NowPlayingModel? = null

    fun show(nowPlaying: NowPlayingModel?) {
        this.nowPlaying = nowPlaying
        invalidateState()
    }

    fun send(command: ListeningRemoteCommand) {
        commandChannel.trySend(command)
    }

    override fun getState(): State {
        val current = nowPlaying
        val builder = State.Builder().setAvailableCommands(availableCommands)
        if (current == null) return builder.setPlaybackState(STATE_IDLE).build()
        val items = current.verses
            .map { verse ->
                val title = "${current.chapterTitle}:${verse.number}"
                MediaItemData
                    .Builder(title)
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setTitle(title)
                            .setArtist(current.subtitle)
                            .setAlbumTitle(current.chapterTitle)
                            .build(),
                    ).setDurationUs(verse.duration.inWholeMicroseconds)
                    .build()
            }.ifEmpty {
                listOf(
                    MediaItemData
                        .Builder(current.chapterTitle)
                        .setMediaMetadata(
                            MediaMetadata
                                .Builder()
                                .setTitle(current.chapterTitle)
                                .setArtist(current.subtitle)
                                .build(),
                        ).build(),
                )
            }
        val positionMs = current.verseElapsed.inWholeMilliseconds
        return builder
            .setPlaylist(items)
            .setCurrentMediaItemIndex(current.verseIndex.coerceIn(items.indices))
            .setContentPositionMs(
                if (current.isPlaying) {
                    PositionSupplier.getExtrapolating(positionMs, 1f)
                } else {
                    PositionSupplier.getConstant(positionMs)
                },
            ).setPlaybackState(STATE_READY)
            .setPlayWhenReady(current.isPlaying, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        send(if (playWhenReady) ListeningRemoteCommand.Play else ListeningRemoteCommand.Pause)
        return Futures.immediateVoidFuture()
    }

    override fun handleSeek(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int,
    ): ListenableFuture<*> {
        send(ListeningRemoteCommand.SkipToVerse(mediaItemIndex))
        return Futures.immediateVoidFuture()
    }

    override fun handleStop(): ListenableFuture<*> {
        send(ListeningRemoteCommand.Stop)
        return Futures.immediateVoidFuture()
    }
}
