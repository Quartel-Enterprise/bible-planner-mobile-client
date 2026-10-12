package com.quare.bibleplanner.feature.read.presentation.listening.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import kotlin.time.Duration

internal class ListeningPlayerUiModelMapper(
    private val getListeningTime: GetListeningTimeUseCase,
) {
    fun map(
        session: ListeningSessionModel,
        speed: Float,
    ): ListeningPlayerUiModel {
        val verse = session.verses.getOrNull(session.verseIndex)
        val verseCount = session.verses.size
        val time = getListeningTime(
            verses = session.verses,
            verseIndex = session.verseIndex,
            verseProgress = session.verseProgress,
            speed = speed,
        )
        return ListeningPlayerUiModel(
            chapter = session.segment.chapter,
            verseNumber = verse?.number,
            verseText = verse?.text?.takeIf(String::isNotBlank),
            spokenWord = session.wordIndex?.let { wordIndex -> verse?.words?.getOrNull(wordIndex)?.target },
            hasWordTimings = verse?.words?.any { word -> word.target is ListeningWordTargetModel.Verse } == true,
            verseIndex = session.verseIndex,
            verseCount = verseCount,
            status = session.status,
            // Why: the verses differ in length (the first carries the chapter title), so progress follows the time.
            progress = if (time.total > Duration.ZERO) (time.elapsed / time.total).toFloat().coerceIn(0f, 1f) else 0f,
            elapsed = time.elapsed,
            remaining = time.remaining,
            total = time.total,
            bibleVersionId = session.bibleVersionId,
            versionAbbreviation = session.bibleVersionId.uppercase(),
            dayProgress = session.day?.let { day ->
                ListeningDayProgressUiModel(
                    chapters = day.segments.map { segment -> segment.chapter },
                    currentIndex = day.segments.indexOf(session.segment).coerceAtLeast(0),
                )
            },
            lockedChapter = session.lockedSegment?.chapter,
        )
    }
}
