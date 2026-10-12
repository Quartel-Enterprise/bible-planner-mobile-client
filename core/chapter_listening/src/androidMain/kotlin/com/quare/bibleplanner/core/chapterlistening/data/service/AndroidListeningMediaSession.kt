package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/*
 * Why: connecting a MediaController is what starts the MediaSessionService; Media3 then moves it to
 * the foreground with the media notification while the chapter plays, which keeps the process alive
 * with the screen off. The ExoPlayer handles audio focus while it plays; the session alone listens
 * for unplugged headphones, which also covers the next chapter loading and the gap before it.
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

    /*
     * Why: the ExoPlayer only keeps the CPU awake while it plays, but between chapters, with the screen
     * off, the next one may still be generated and downloaded; the session holds it for as long as it
     * reads as playing.
     */
    private val wakeLock: PowerManager.WakeLock by lazy {
        context
            .getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
            .apply { setReferenceCounted(false) }
    }

    // Why: a long safety net, since nothing is published to renew it while the next chapter loads for minutes.
    private val wakeLockTimeout: Duration = 30.minutes

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
        if (!nowPlaying.isPlaying) {
            releaseWakeLock()
        } else if (!wakeLock.isHeld) {
            wakeLock.acquire(wakeLockTimeout.inWholeMilliseconds)
        }
    }

    override fun deactivate() {
        player?.show(null)
        releaseWakeLock()
        val future = controllerFuture ?: return
        MediaController.releaseFuture(future)
        controllerFuture = null
        context.unregisterReceiver(noisyReceiver)
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    private companion object {
        const val WAKE_LOCK_TAG = "BiblePlanner:ChapterListening"
    }
}
