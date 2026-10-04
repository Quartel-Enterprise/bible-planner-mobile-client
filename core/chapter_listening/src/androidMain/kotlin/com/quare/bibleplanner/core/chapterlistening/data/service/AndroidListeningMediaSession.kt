package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/*
 * Why: connecting a MediaController is what starts the MediaSessionService; Media3 then moves it to
 * the foreground with the media notification while speech plays, which keeps the process alive with
 * the screen off. The player is not an ExoPlayer, so audio focus and the noisy broadcast are handled here.
 */
internal class AndroidListeningMediaSession(
    private val context: Context,
    private val player: ListeningPlayer,
) : ListeningMediaSession {
    override val commands: Flow<ListeningRemoteCommand> = player.commands

    private val interruptionChannel = Channel<AudioInterruptionModel>(Channel.UNLIMITED)
    override val interruptions: Flow<AudioInterruptionModel> = interruptionChannel.receiveAsFlow()

    private val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)
    private val focusRequest: AudioFocusRequest = AudioFocusRequest
        .Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        ).setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(::onAudioFocusChange)
        .build()
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            interruptionChannel.trySend(AudioInterruptionModel.PAUSE_REQUESTED)
        }
    }
    private var hasAudioFocus = false
    private var isNoisyReceiverRegistered = false
    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun requestActivation(): Boolean {
        if (!hasAudioFocus) {
            hasAudioFocus = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        if (!hasAudioFocus) return false
        if (!isNoisyReceiverRegistered) {
            ContextCompat.registerReceiver(
                context,
                noisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            isNoisyReceiverRegistered = true
        }
        if (controllerFuture == null) {
            controllerFuture = MediaController
                .Builder(
                    context,
                    SessionToken(context, ComponentName(context, ChapterListeningService::class.java)),
                ).buildAsync()
        }
        return true
    }

    override fun update(nowPlaying: NowPlayingModel) {
        player.show(nowPlaying)
    }

    override fun deactivate() {
        player.show(null)
        if (hasAudioFocus) {
            audioManager.abandonAudioFocusRequest(focusRequest)
            hasAudioFocus = false
        }
        if (isNoisyReceiverRegistered) {
            context.unregisterReceiver(noisyReceiver)
            isNoisyReceiverRegistered = false
        }
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
    }

    private fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                interruptionChannel.trySend(AudioInterruptionModel.PAUSE_REQUESTED)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            -> interruptionChannel.trySend(AudioInterruptionModel.BEGAN)

            AudioManager.AUDIOFOCUS_GAIN -> interruptionChannel.trySend(AudioInterruptionModel.ENDED)
        }
    }
}
