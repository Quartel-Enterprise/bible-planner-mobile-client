package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.store.ChapterStudyStatusPrefetchStore

internal class ChapterStudyQuotaChecker(
    private val repository: ChapterStudyRepository,
    private val generationCoordinator: ChapterStudyGenerationCoordinator,
    private val statusPrefetchStore: ChapterStudyStatusPrefetchStore,
) {
    /*
     * Why: a prefetched status is only trusted when it opens the study. A stale "open" is caught
     * by the study screen and the server's own limit check, while a stale "limit reached" would
     * show the unlock offer to someone who still has free studies, so that one is always confirmed.
     */
    fun isOpenedByPrefetchedStatus(
        key: ChapterStudyStatusKey,
        target: ChapterStudyTargetModel,
    ): Boolean = statusPrefetchStore.find(key)?.let { status ->
        hasFreeStudyLeft(
            status = status,
            target = target,
        )
    } == true

    suspend fun fetchAndKeepStatus(key: ChapterStudyStatusKey): ChapterStudyStatusModel? =
        statusPrefetchStore.fetchAndKeep(key) {
            repository.fetchStatus(
                chapter = key.scope.chapter,
                languageCode = key.scope.languageCode,
            )
        }

    fun hasFreeStudyLeft(
        status: ChapterStudyStatusModel,
        target: ChapterStudyTargetModel,
    ): Boolean = status.isUnlocked ||
        status.usedCount + generationCoordinator.getGeneratingCount(excluding = target) < status.freeLimit
}
