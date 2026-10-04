package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class NowPlayingModel(
    val chapterTitle: String,
    val subtitle: String,
    val verses: List<NowPlayingVerseModel>,
    val verseIndex: Int,
    val verseElapsed: Duration,
    val isPlaying: Boolean,
)
