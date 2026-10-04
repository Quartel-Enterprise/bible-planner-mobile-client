package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface ChapterListeningEventModel {
    data object SleepTimerEnded : ChapterListeningEventModel
}
