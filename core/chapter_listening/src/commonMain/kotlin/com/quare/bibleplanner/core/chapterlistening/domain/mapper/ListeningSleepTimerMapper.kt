package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import kotlin.time.Duration.Companion.minutes

internal fun ListeningSleepTimerOption.toSleepTimerModel(): ListeningSleepTimerModel = when (this) {
    ListeningSleepTimerOption.OFF -> ListeningSleepTimerModel.Off

    ListeningSleepTimerOption.FIFTEEN_MINUTES -> ListeningSleepTimerModel.Countdown(
        option = this,
        remaining = 15.minutes,
    )

    ListeningSleepTimerOption.THIRTY_MINUTES -> ListeningSleepTimerModel.Countdown(
        option = this,
        remaining = 30.minutes,
    )

    ListeningSleepTimerOption.END_OF_CHAPTER -> ListeningSleepTimerModel.EndOfChapter
}
