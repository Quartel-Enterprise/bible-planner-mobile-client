package com.quare.bibleplanner.core.daystudy.data.model

import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyResponseDto

internal sealed interface DayStudyStreamEvent {
    data class Progress(
        val phase: String,
    ) : DayStudyStreamEvent

    data class Complete(
        val response: DayStudyResponseDto,
    ) : DayStudyStreamEvent
}
