package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class ChapterAudioModel(
    val audioPath: String,
    val verses: List<ChapterAudioVerseModel>,
    val duration: Duration,
)
