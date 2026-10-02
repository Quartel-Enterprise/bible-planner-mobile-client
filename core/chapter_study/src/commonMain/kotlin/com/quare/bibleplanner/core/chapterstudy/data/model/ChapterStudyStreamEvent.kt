package com.quare.bibleplanner.core.chapterstudy.data.model

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyResponseDto

internal sealed interface ChapterStudyStreamEvent {
    data class Progress(
        val phase: String,
    ) : ChapterStudyStreamEvent

    data class Complete(
        val response: ChapterStudyResponseDto,
    ) : ChapterStudyStreamEvent
}
