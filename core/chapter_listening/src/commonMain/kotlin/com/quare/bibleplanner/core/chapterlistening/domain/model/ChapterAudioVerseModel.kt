package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class ChapterAudioVerseModel(
    val number: Int,
    val headingStart: Duration?,
    val start: Duration,
    val end: Duration,
)
