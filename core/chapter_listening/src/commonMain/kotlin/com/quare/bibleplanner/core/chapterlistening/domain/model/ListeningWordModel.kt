package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

data class ListeningWordModel(
    val target: ListeningWordTargetModel,
    val start: Duration,
    val end: Duration,
)
