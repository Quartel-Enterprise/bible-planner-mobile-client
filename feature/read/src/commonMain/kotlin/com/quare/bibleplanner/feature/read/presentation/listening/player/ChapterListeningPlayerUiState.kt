package com.quare.bibleplanner.feature.read.presentation.listening.player

import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel

data class ChapterListeningPlayerUiState(
    val player: ListeningPlayerUiModel?,
    val previousChapter: ChapterLocationModel?,
    val nextChapter: ChapterLocationModel?,
    val speed: Float,
    val voices: ListeningVoicesUiModel,
    val sleepTimer: ListeningSleepTimerUiModel,
    val isAutoNextEnabled: Boolean,
    val isAutoNextLocked: Boolean,
)
