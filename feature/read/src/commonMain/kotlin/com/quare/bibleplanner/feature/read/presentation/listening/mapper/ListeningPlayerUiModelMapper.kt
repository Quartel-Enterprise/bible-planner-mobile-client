package com.quare.bibleplanner.feature.read.presentation.listening.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningDayProgressUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel

internal class ListeningPlayerUiModelMapper(
    private val estimateListeningTime: EstimateListeningTimeUseCase,
) {
    fun map(
        session: ListeningSessionModel,
        speed: Float,
    ): ListeningPlayerUiModel {
        val verse = session.verses.getOrNull(session.verseIndex)
        val verseCount = session.verses.size
        val time = estimateListeningTime(
            wordCounts = session.verses.map(ListeningVerseModel::wordCount),
            verseIndex = session.verseIndex,
            verseProgress = session.verseProgress,
            speed = speed,
        )
        return ListeningPlayerUiModel(
            chapter = session.segment.chapter,
            verseNumber = verse?.number,
            verseText = verse?.text,
            verseIndex = session.verseIndex,
            verseCount = verseCount,
            status = session.status,
            progress = getProgress(session),
            elapsed = time.elapsed,
            remaining = time.remaining,
            total = time.total,
            versionAbbreviation = session.bibleVersionId.uppercase(),
            languageTag = session.languageTag,
            dayProgress = session.day?.let { day ->
                ListeningDayProgressUiModel(
                    chapters = day.segments.map { segment -> segment.chapter },
                    currentIndex = day.segments.indexOf(session.segment).coerceAtLeast(0),
                )
            },
            lockedChapter = session.lockedSegment?.chapter,
        )
    }

    private fun getProgress(session: ListeningSessionModel): Float {
        val verseCount = session.verses.size
        if (verseCount == 0) return 0f
        return ((session.verseIndex + session.verseProgress) / verseCount).coerceIn(0f, 1f)
    }
}
