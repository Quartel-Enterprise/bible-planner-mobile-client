package com.quare.bibleplanner.core.chapterlistening.data.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.R
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import org.koin.android.ext.android.inject

@OptIn(UnstableApi::class)
internal class ChapterListeningService :
    MediaSessionService(),
    MediaSession.Callback {
    private val player: ListeningPlayer by inject()
    private val previousChapterCommand = SessionCommand(PREVIOUS_CHAPTER_ACTION, Bundle.EMPTY)
    private val nextChapterCommand = SessionCommand(NEXT_CHAPTER_ACTION, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this).apply { setSmallIcon(R.drawable.ic_listening_notification) },
        )
        mediaSession = MediaSession
            .Builder(this, player)
            .setId(SESSION_ID)
            .setCallback(this)
            .setMediaButtonPreferences(
                listOf(
                    createChapterButton(
                        icon = CommandButton.ICON_REWIND,
                        label = getString(R.string.listening_previous_chapter),
                        command = previousChapterCommand,
                        slot = CommandButton.SLOT_BACK_SECONDARY,
                    ),
                    createChapterButton(
                        icon = CommandButton.ICON_FAST_FORWARD,
                        label = getString(R.string.listening_next_chapter),
                        command = nextChapterCommand,
                        slot = CommandButton.SLOT_FORWARD_SECONDARY,
                    ),
                ),
            ).apply { createLaunchIntent()?.let(::setSessionActivity) }
            .build()
            .also(::addSession)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult = MediaSession.ConnectionResult
        .AcceptedResultBuilder(session, controller)
        .setAvailableSessionCommands(
            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                .buildUpon()
                .add(previousChapterCommand)
                .add(nextChapterCommand)
                .build(),
        ).build()

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        val command = when (customCommand.customAction) {
            PREVIOUS_CHAPTER_ACTION -> ListeningRemoteCommand.PreviousChapter
            NEXT_CHAPTER_ACTION -> ListeningRemoteCommand.NextChapter
            else -> null
        }
        command?.let(player::send)
        val resultCode = if (command == null) SessionResult.RESULT_ERROR_NOT_SUPPORTED else SessionResult.RESULT_SUCCESS
        return Futures.immediateFuture(SessionResult(resultCode))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        player.send(ListeningRemoteCommand.Stop)
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun createChapterButton(
        icon: Int,
        label: String,
        command: SessionCommand,
        slot: Int,
    ): CommandButton = CommandButton
        .Builder(icon)
        .setDisplayName(label)
        .setSessionCommand(command)
        .setSlots(slot)
        .build()

    private fun createLaunchIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        const val SESSION_ID = "chapter_listening"
        const val PREVIOUS_CHAPTER_ACTION = "com.quare.bibleplanner.listening.PREVIOUS_CHAPTER"
        const val NEXT_CHAPTER_ACTION = "com.quare.bibleplanner.listening.NEXT_CHAPTER"
    }
}
