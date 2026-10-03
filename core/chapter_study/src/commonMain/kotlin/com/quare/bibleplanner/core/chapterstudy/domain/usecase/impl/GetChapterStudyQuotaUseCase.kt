package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyQuota
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.first

internal class GetChapterStudyQuotaUseCase(
    private val repository: ChapterStudyRepository,
    private val scopeResolver: ChapterStudyScopeResolver,
    private val generationCoordinator: ChapterStudyGenerationCoordinator,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
) : GetChapterStudyQuota {
    override suspend fun invoke(target: ChapterStudyTargetModel): ChapterStudyQuotaModel? {
        if (observeAuthenticatedUserId().first() == null) return null
        val scope = scopeResolver.resolve(target)
        val status = repository.fetchStatus(
            chapter = scope.chapter,
            languageCode = scope.languageCode,
        ) ?: return null
        val spentCount = status.usedCount + generationCoordinator.getGeneratingCount(excluding = target)
        return ChapterStudyQuotaModel(
            freeLimit = status.freeLimit,
            remainingFree = (status.freeLimit - spentCount).coerceAtLeast(0),
            isUnlocked = status.isUnlocked,
            rewardedRemainingToday = status.rewardedRemainingToday,
        )
    }
}
