package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class SpeechUtteranceModel(
    val id: String,
    val text: String,
    val leadingSilence: Duration,
)
