package com.quare.bibleplanner.core.chapterlistening.domain.controller

import kotlin.time.Duration

internal class PlayCall(
    val audioPath: String,
    val position: Duration,
    val playbackId: Int,
)
