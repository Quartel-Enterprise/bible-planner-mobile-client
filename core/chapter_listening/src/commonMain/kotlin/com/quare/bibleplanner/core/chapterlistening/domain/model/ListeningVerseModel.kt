package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class ListeningVerseModel(
    val number: Int,
    val text: String,
    val start: Duration,
    val end: Duration,
    val words: List<ListeningWordModel>,
)
