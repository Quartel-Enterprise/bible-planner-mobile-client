package com.quare.bibleplanner.feature.read.presentation.listening.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

data class ListeningDayProgressUiModel(
    val chapters: List<ChapterLocationModel>,
    val currentIndex: Int,
)
