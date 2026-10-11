package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.flow.Flow

/*
 * Why: connecting a MediaController is what starts the MediaSessionService; Media3 then moves it to
 * the foreground with the media notification while the chapter plays, which keeps the process alive
 * with the screen off. The ExoPlayer handles audio focus and unplugged headphones while it plays;
 * the session listens for unplugged headphones on its own too, since the ExoPlayer is paused while
 * the next chapter loads or during the gap before it.
 */
internal class AndroidListeningMediaSession(
    private val context: Context,
    private val createPlayer: () -> ListeningPlayer,
    private val events: ListeningPlayerEvents,
) : ListeningMediaSession {
    override val commands: Flow<ListeningRemoteCommand> = events.commands
    override val interruptions: Flow<AudioInterruptionModel> = events.interruptions

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            events.interrupt(AudioInterruptionModel.PAUSE_REQUESTED)
        }
    }

    // Why: built with the first chapter shown, so opening the reader alone never creates the ExoPlayer.
    private var player: ListeningPlayer? = null

    override fun requestActivation(): Boolean {
        if (controllerFuture == null) {
            controllerFuture = MediaController
                .Builder(
                    context,
                    SessionToken(context, ComponentName(context, ChapterListeningService::class.java)),
                ).buildAsync()
            ContextCompat.registerReceiver(
                context,
                noisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }
        return true
    }

    override fun update(nowPlaying: NowPlayingModel) {
        val shownPlayer = player ?: createPlayer().also { created -> player = created }
        shownPlayer.show(nowPlaying)
    }

    override fun deactivate() {
        player?.show(null)
        val future = controllerFuture ?: return
        MediaController.releaseFuture(future)
        controllerFuture = null
        context.unregisterReceiver(noisyReceiver)
    }
}
