package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class ChapterAudioWordModel(
    val offset: Int,
    val start: Duration,
    val end: Duration,
)
