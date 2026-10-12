package com.quare.bibleplanner.feature.read.fixture

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun listeningSession(
    chapter: ChapterLocationModel,
    status: ListeningStatusModel = ListeningStatusModel.PLAYING,
    cause: ChapterChangeCause = ChapterChangeCause.START,
    finishedChapter: ChapterLocationModel? = null,
    lockedChapter: ChapterLocationModel? = null,
    day: ListeningDayModel? = null,
    verseIndex: Int = 0,
    wordIndex: Int? = null,
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
        shouldForceCanonOrder = false,
        verses = listOf(
            ListeningVerseModel(
                number = 1,
                text = "one two three four five",
                start = Duration.ZERO,
                end = 2.seconds,
                words = listOf(
                    ListeningWordModel(
                        target = ListeningWordTargetModel.ChapterBook,
                        start = 0.1.seconds,
                        end = 0.2.seconds,
                    ),
                    ListeningWordModel(
                        target = ListeningWordTargetModel.Verse(0..2),
                        start = 0.2.seconds,
                        end = 0.5.seconds,
                    ),
                    ListeningWordModel(
                        target = ListeningWordTargetModel.Verse(4..6),
                        start = 0.5.seconds,
                        end = 0.8.seconds,
                    ),
                ),
            ),
            ListeningVerseModel(
                number = 2,
                text = "six seven eight nine ten",
                start = 2.seconds,
                end = 4.seconds,
                words = emptyList(),
            ),
        ),
        verseIndex = verseIndex,
        verseProgress = 0.5f,
        wordIndex = wordIndex,
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
