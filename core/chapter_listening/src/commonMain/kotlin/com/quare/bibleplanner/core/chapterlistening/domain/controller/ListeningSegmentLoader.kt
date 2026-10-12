package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toListeningStatus
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toUnloaded
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersesUseCase
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.time.Duration

internal class ListeningSegmentLoader(
    private val store: ChapterListeningStateStore,
    private val playback: ListeningPlayback,
    private val prefetcher: ChapterAudioPrefetcher,
    private val cacheKeeper: ChapterAudioCacheKeeper,
    private val chapterAudioRepository: ChapterAudioRepository,
    private val getListeningVerses: GetListeningVersesUseCase,
    private val tracker: ChapterListeningAnalyticsTracker,
) {
    // Why: a retry after a failed playback picks up where the chapter was, not at its title.
    private var retryStart: Pair<ListeningSegmentModel, Duration>? = null

    // Why: returns the session being left, whose revision is checked once leaving it can't compete with a download.
    fun reset(
        session: ListeningSessionModel,
        status: ListeningStatusModel,
    ): ListeningSessionModel? {
        prefetcher.cancelUnlessFor(session.segment.chapter)
        val left = findLeftSession(session.segment.chapter)
        playback.reset()
        store.update { it.copy(session = session.toUnloaded(status)) }
        return left
    }

    suspend fun load(
        session: ListeningSessionModel,
        leftSession: ListeningSessionModel?,
    ): ListeningStatusModel {
        cacheKeeper.awaitEviction()
        val result = chapterAudioRepository.loadChapterAudio(
            bibleVersionId = session.bibleVersionId,
            chapter = session.segment.chapter,
        )
        // Why: checked only now, so a new revision of the left chapter doesn't compete with this download.
        leftSession?.let(cacheKeeper::refreshLeftChapter)
        val loadedAudio = (result as? ChapterAudioResultModel.Ready)?.audio
        val verses = loadedAudio
            ?.let { chapterAudio ->
                getListeningVerses(
                    audio = chapterAudio,
                    session = session,
                )
            }.orEmpty()
        val statusWhileLoading = store.session?.status
        val status = when {
            result is ChapterAudioResultModel.Failed -> result.failure.toListeningStatus()

            // Why: a manifest with none of the segment's verses has nothing to play, which is a broken audio.
            verses.isEmpty() -> ListeningStatusModel.AUDIO_UNAVAILABLE

            // Why: a play, pause or audio interruption that arrived while loading wins over how the load started.
            statusWhileLoading == ListeningStatusModel.PREPARING ||
                statusWhileLoading == ListeningStatusModel.INTERRUPTED -> statusWhileLoading

            else -> ListeningStatusModel.PAUSED
        }
        if (loadedAudio != null && verses.isEmpty()) cacheKeeper.evictEmpty(loadedAudio.audioPath)
        playback.audio = loadedAudio
        playback.position = toStartPosition(
            segment = session.segment,
            verses = verses,
        )
        store.updateSession {
            it.copy(
                verses = verses,
                status = status,
            )
        }
        tracker.trackOpened(
            session = session,
            result = result,
            status = status,
        )
        return status
    }

    fun rememberRetryStart(segment: ListeningSegmentModel) {
        retryStart = segment to playback.position
    }

    fun forgetRetryStart() {
        retryStart = null
    }

    private fun findLeftSession(nextChapter: ChapterLocationModel): ListeningSessionModel? {
        val left = store.session ?: return null
        return left.takeIf { playback.audio != null && left.segment.chapter != nextChapter }
    }

    private fun toStartPosition(
        segment: ListeningSegmentModel,
        verses: List<ListeningVerseModel>,
    ): Duration {
        val first = verses.firstOrNull() ?: return Duration.ZERO
        val retried = retryStart?.takeIf { (retriedSegment, _) -> retriedSegment == segment }?.second
        retryStart = null
        return retried?.coerceIn(first.start, verses.last().end) ?: first.start
    }
}
