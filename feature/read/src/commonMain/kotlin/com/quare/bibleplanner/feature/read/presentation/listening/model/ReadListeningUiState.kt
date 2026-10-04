package com.quare.bibleplanner.feature.read.presentation.listening.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

data class ReadListeningUiState(
    val isAvailable: Boolean,
    val speed: Float,
    val todayChapters: List<ChapterLocationModel>,
    val player: ListeningPlayerUiModel?,
    val finishOffer: ListeningFinishOfferUiModel?,
)
