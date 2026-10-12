package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

internal class ListeningSleepTimerMapperTest {
    @Test
    fun `GIVEN each sleep timer option WHEN mapping it THEN starts the matching timer`() {
        // Given
        val options = ListeningSleepTimerOption.entries

        // When
        val timers = options.associateWith(ListeningSleepTimerOption::toSleepTimerModel)

        // Then
        assertEquals(
            mapOf(
                ListeningSleepTimerOption.OFF to ListeningSleepTimerModel.Off,
                ListeningSleepTimerOption.FIFTEEN_MINUTES to ListeningSleepTimerModel.Countdown(
                    option = ListeningSleepTimerOption.FIFTEEN_MINUTES,
                    remaining = 15.minutes,
                ),
                ListeningSleepTimerOption.THIRTY_MINUTES to ListeningSleepTimerModel.Countdown(
                    option = ListeningSleepTimerOption.THIRTY_MINUTES,
                    remaining = 30.minutes,
                ),
                ListeningSleepTimerOption.END_OF_CHAPTER to ListeningSleepTimerModel.EndOfChapter,
            ),
            timers,
        )
    }
}
