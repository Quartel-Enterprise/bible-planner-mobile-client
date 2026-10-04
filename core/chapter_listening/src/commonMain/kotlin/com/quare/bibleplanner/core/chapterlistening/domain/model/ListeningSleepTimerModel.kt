package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

sealed interface ListeningSleepTimerModel {
    data object Off : ListeningSleepTimerModel

    data class Countdown(
        val option: ListeningSleepTimerOption,
        val remaining: Duration,
    ) : ListeningSleepTimerModel

    data object EndOfChapter : ListeningSleepTimerModel
}
