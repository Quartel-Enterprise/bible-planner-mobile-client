package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

/*
 * Why: ExoPlayer owns audio focus, the headphones-unplugged pause and the wake lock; it is created on
 * the main looper because the controller and the media session both drive it from there.
 */
@OptIn(UnstableApi::class)
internal fun createListeningExoPlayer(context: Context): ExoPlayer = ExoPlayer
    .Builder(context)
    .setLooper(Looper.getMainLooper())
    .setAudioAttributes(
        AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build(),
        true,
    ).setHandleAudioBecomingNoisy(true)
    .setWakeMode(C.WAKE_MODE_LOCAL)
    .build()
