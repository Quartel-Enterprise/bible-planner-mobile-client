package com.quare.bibleplanner.feature.read.presentation.listening.model

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.time.Duration

data class ListeningPlayerUiModel(
    val chapter: ChapterLocationModel,
    val verseNumber: Int?,
    val verseText: String?,
    val verseIndex: Int,
    val verseCount: Int,
    val status: ListeningStatusModel,
    val progress: Float,
    val elapsed: Duration,
    val remaining: Duration,
    val total: Duration,
    val versionAbbreviation: String,
    val languageTag: String,
    val dayProgress: ListeningDayProgressUiModel?,
    val lockedChapter: ChapterLocationModel?,
)
