package com.quare.bibleplanner.feature.read.presentation.listening.player

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import kotlin.time.Duration

data class ListeningSleepTimerUiModel(
    val selectedOption: ListeningSleepTimerOption,
    val remaining: Duration?,
)
