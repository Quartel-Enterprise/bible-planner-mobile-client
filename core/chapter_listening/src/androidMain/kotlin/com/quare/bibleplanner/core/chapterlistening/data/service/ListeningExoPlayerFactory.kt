package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp3.Mp3Extractor

/*
 * Why: ExoPlayer owns audio focus and the wake lock (unplugged headphones are the session's, which
 * also covers the gap between chapters); it is created on
 * the main looper because the controller and the media session both drive it from there. Verse skips
 * seek to the exact millisecond, which an MP3 only allows with index seeking (iOS seeks exactly too).
 */
@OptIn(UnstableApi::class)
internal fun createListeningExoPlayer(context: Context): ExoPlayer = ExoPlayer
    .Builder(
        context,
        DefaultMediaSourceFactory(
            context,
            DefaultExtractorsFactory().setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING),
        ),
    ).setLooper(Looper.getMainLooper())
    .setAudioAttributes(
        AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build(),
        true,
    ).setWakeMode(C.WAKE_MODE_LOCAL)
    .build()
