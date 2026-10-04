package com.quare.bibleplanner.core.chapterlistening.domain.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

data class ListeningSessionModel(
    val mode: ListeningModeModel,
    val day: ListeningDayModel?,
    val segment: ListeningSegmentModel,
    val bibleVersionId: String,
    val languageTag: String,
    val shouldForceCanonOrder: Boolean,
    val verses: List<ListeningVerseModel>,
    val verseIndex: Int,
    val verseProgress: Float,
    val status: ListeningStatusModel,
    val chapterChangeCause: ChapterChangeCause,
    val finishedChapter: ChapterLocationModel?,
    val lockedSegment: ListeningSegmentModel?,
)
