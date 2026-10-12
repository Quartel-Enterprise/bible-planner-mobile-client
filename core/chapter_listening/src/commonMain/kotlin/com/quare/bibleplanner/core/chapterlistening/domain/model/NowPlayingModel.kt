package com.quare.bibleplanner.core.chapterlistening.domain.model

data class NowPlayingModel(
    val chapterTitle: String,
    val subtitle: String,
    val verseNumber: Int?,
    val time: ListeningTimeModel,
    val isAudioLoaded: Boolean,
    val isPlaying: Boolean,
)
