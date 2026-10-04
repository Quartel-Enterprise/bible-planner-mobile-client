package com.quare.bibleplanner.feature.read.presentation.listening.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

data class ListeningFinishOfferUiModel(
    val chapter: ChapterLocationModel,
    val playingChapter: ChapterLocationModel?,
)
