package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class ChapterAudioCacheKeeper(
    private val scope: CoroutineScope,
    private val chapterAudioRepository: ChapterAudioRepository,
) {
    private val evictedEmptyAudioPaths = mutableSetOf<String>()
    private var refreshJob: Job? = null
    private var evictJob: Job? = null

    /*
     * Why: a cached chapter is checked once per launch for a corrected revision, which plays from the
     * next time on; it is checked once the session leaves it, so it never competes with its own playback.
     */
    fun refreshLeftChapter(left: ListeningSessionModel) {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            chapterAudioRepository.refresh(
                bibleVersionId = left.bibleVersionId,
                chapter = left.segment.chapter,
            )
        }
    }

    /*
     * Why: dropped from the cache, so a retry fetches the server's manifest instead of the same one;
     * once per file and launch, since a revision whose verses don't match would download again on each retry.
     */
    fun evictEmpty(audioPath: String) {
        if (evictedEmptyAudioPaths.add(audioPath)) evict(audioPath)
    }

    // Why: a cached file the player can't decode would fail every retry, so it is downloaded again.
    fun evict(audioPath: String) {
        evictJob = scope.launch { chapterAudioRepository.evict(audioPath) }
    }

    // Why: a retry waits for this, so it can't find the broken file still in the cache.
    suspend fun awaitEviction() {
        evictJob?.join()
    }

    fun cancelRefresh() {
        refreshJob?.cancel()
    }
}
