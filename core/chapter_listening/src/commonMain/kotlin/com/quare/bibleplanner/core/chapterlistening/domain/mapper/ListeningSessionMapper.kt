package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

internal fun createListeningSession(
    mode: ListeningModeModel,
    day: ListeningDayModel?,
    segment: ListeningSegmentModel,
    bibleVersionId: String,
    shouldForceCanonOrder: Boolean,
): ListeningSessionModel = ListeningSessionModel(
    mode = mode,
    day = day,
    segment = segment,
    bibleVersionId = bibleVersionId,
    shouldForceCanonOrder = shouldForceCanonOrder,
    verses = emptyList(),
    verseIndex = 0,
    verseProgress = 0f,
    wordIndex = null,
    status = ListeningStatusModel.PREPARING,
    chapterChangeCause = ChapterChangeCause.START,
    finishedChapter = null,
    lockedSegment = null,
)

internal fun ListeningSessionModel.toReaderSession(chapter: ChapterLocationModel): ListeningSessionModel {
    val daySegment = day?.segments?.find { it.chapter == chapter }
    val base = if (daySegment == null) {
        copy(
            mode = ListeningModeModel.CHAPTER,
            day = null,
        )
    } else {
        this
    }
    return base.copy(
        segment = daySegment ?: chapter.toWholeChapterSegment(),
        chapterChangeCause = ChapterChangeCause.READER,
    )
}

internal fun ListeningSessionModel.toUnloaded(status: ListeningStatusModel): ListeningSessionModel = copy(
    verses = emptyList(),
    verseIndex = 0,
    verseProgress = 0f,
    wordIndex = null,
    status = status,
    lockedSegment = null,
)

// Why: the verses belong to the audio that failed, and the system player must not offer to seek in it.
internal fun ListeningSessionModel.toPlaybackFailed(): ListeningSessionModel = copy(
    status = ListeningStatusModel.AUDIO_UNAVAILABLE,
    verses = emptyList(),
    verseIndex = 0,
    verseProgress = 0f,
    wordIndex = null,
)
