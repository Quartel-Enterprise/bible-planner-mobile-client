package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.time.Duration.Companion.seconds

internal class ListeningNowPlayingMapper(
    private val getListeningChapterTitle: GetListeningChapterTitle,
    private val getListeningTime: GetListeningTimeUseCase,
) {
    private val chapterTitles = mutableMapOf<ChapterLocationModel, String>()

    suspend fun toNowPlayingOrNull(state: ChapterListeningStateModel): NowPlayingModel? {
        val current = state.session ?: return null
        val chapterTitle = chapterTitles.getOrPut(current.segment.chapter) {
            getListeningChapterTitle(current.segment.chapter)
        }
        val time = getListeningTime(
            verses = current.verses,
            verseIndex = current.verseIndex,
            verseProgress = current.verseProgress,
            speed = state.settings.speed,
        )
        return NowPlayingModel(
            chapterTitle = chapterTitle,
            subtitle = current.bibleVersionId.uppercase(),
            verseNumber = current.verses.getOrNull(current.verseIndex)?.number,
            isAudioLoaded = current.verses.isNotEmpty(),
            // Why: whole seconds, so the 200 ms position ticks don't republish the system player every time.
            time = time.copy(elapsed = time.elapsed.inWholeSeconds.seconds),
            // Why: a chapter change passes through PREPARING, and reporting it as paused would drop the foreground service.
            isPlaying = current.isPlaying,
        )
    }
}
