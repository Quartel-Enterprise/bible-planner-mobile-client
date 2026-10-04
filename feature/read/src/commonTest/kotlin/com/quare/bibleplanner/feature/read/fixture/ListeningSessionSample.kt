package com.quare.bibleplanner.feature.read.fixture

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

internal fun listeningSession(
    chapter: ChapterLocationModel,
    status: ListeningStatusModel = ListeningStatusModel.PLAYING,
    cause: ChapterChangeCause = ChapterChangeCause.START,
    finishedChapter: ChapterLocationModel? = null,
    lockedChapter: ChapterLocationModel? = null,
    day: ListeningDayModel? = null,
    verseIndex: Int = 0,
): ListeningSessionModel {
    val segment = ListeningSegmentModel(
        chapter = chapter,
        startVerse = null,
        endVerse = null,
    )
    return ListeningSessionModel(
        mode = if (day == null) ListeningModeModel.CHAPTER else ListeningModeModel.DAY_READING,
        day = day,
        segment = segment,
        bibleVersionId = "web",
        languageTag = "en-US",
        shouldForceCanonOrder = false,
        verses = listOf(
            ListeningVerseModel(number = 1, text = "one two three four five", wordCount = 5),
            ListeningVerseModel(number = 2, text = "six seven eight nine ten", wordCount = 5),
        ),
        verseIndex = verseIndex,
        verseProgress = 0.5f,
        status = status,
        chapterChangeCause = cause,
        finishedChapter = finishedChapter,
        lockedSegment = lockedChapter?.let {
            ListeningSegmentModel(
                chapter = it,
                startVerse = null,
                endVerse = null,
            )
        },
    )
}
