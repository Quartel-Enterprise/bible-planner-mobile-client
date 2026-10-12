package com.quare.bibleplanner.core.chapterlistening.domain.model

import com.quare.bibleplanner.core.model.book.ChapterLocationModel

data class ListeningSessionModel(
    val mode: ListeningModeModel,
    val day: ListeningDayModel?,
    val segment: ListeningSegmentModel,
    val bibleVersionId: String,
    val shouldForceCanonOrder: Boolean,
    val verses: List<ListeningVerseModel>,
    val verseIndex: Int,
    val verseProgress: Float,
    val wordIndex: Int?,
    val status: ListeningStatusModel,
    val chapterChangeCause: ChapterChangeCause,
    val finishedChapter: ChapterLocationModel?,
    val lockedSegment: ListeningSegmentModel?,
) {
    // Why: a chapter still loading counts as playing, since it starts on its own once loaded.
    val isPlaying: Boolean
        get() = status == ListeningStatusModel.PLAYING || status == ListeningStatusModel.PREPARING
}
