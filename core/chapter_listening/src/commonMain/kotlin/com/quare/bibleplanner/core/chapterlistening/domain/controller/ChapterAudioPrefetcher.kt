package com.quare.bibleplanner.core.chapterlistening.domain.controller

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.FindAdjacentListeningSegmentUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.isOpen
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * Why: the next chapter is downloaded (and generated, the first time anyone asks for it) while
 * this one plays, so moving on doesn't wait for it; a locked one is left alone.
 */
internal class ChapterAudioPrefetcher(
    private val scope: CoroutineScope,
    private val chapterAudioRepository: ChapterAudioRepository,
    private val findAdjacentSegment: FindAdjacentListeningSegmentUseCase,
    private val getChapterListeningAccess: GetChapterListeningAccess,
) {
    private var prefetchJob: Job? = null
    private var prefetchingChapter: ChapterLocationModel? = null

    fun prefetchNext(current: ListeningSessionModel) {
        prefetchJob?.cancel()
        prefetchingChapter = null
        prefetchJob = scope.launch {
            try {
                suspendRunCatching { prefetch(current) }
                    .onFailure { throwable -> Logger.w(throwable) { "Could not prefetch the next chapter audio" } }
            } finally {
                // Why: a cancelled job ends after its replacement started, and must not clear the new one's chapter.
                if (prefetchJob === coroutineContext[Job]) prefetchingChapter = null
            }
        }
    }

    // Why: a prefetch for a chapter that is no longer next would compete with the one now loading.
    fun cancelUnlessFor(chapter: ChapterLocationModel) {
        if (prefetchingChapter == chapter) return
        prefetchJob?.cancel()
        prefetchingChapter = null
    }

    fun cancel() {
        prefetchJob?.cancel()
    }

    private suspend fun prefetch(current: ListeningSessionModel) {
        val next = findAdjacentSegment(
            current = current,
            direction = ChapterDirectionModel.NEXT,
        ) ?: return
        prefetchingChapter = next.chapter
        if (!getChapterListeningAccess.isOpen(next)) return
        chapterAudioRepository.loadChapterAudio(
            bibleVersionId = current.bibleVersionId,
            chapter = next.chapter,
        )
    }
}
