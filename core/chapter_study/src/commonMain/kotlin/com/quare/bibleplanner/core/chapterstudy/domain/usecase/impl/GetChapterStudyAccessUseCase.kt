package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.first

internal class GetChapterStudyAccessUseCase(
    private val repository: ChapterStudyRepository,
    private val scopeResolver: ChapterStudyScopeResolver,
    private val generationCoordinator: ChapterStudyGenerationCoordinator,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val observeIsProUser: ObserveIsProUser,
) : GetChapterStudyAccess {
    override suspend fun invoke(target: ChapterStudyTargetModel): ChapterStudyAccessModel {
        val scope = scopeResolver.resolve(target)
        val hasLocalStudy = repository.findCachedStudy(
            chapter = scope.chapter,
            languageCode = scope.languageCode,
        ) != null
        if (hasLocalStudy) return ChapterStudyAccessModel.OPEN
        if (observeAuthenticatedUserId().first() == null) return ChapterStudyAccessModel.LOGIN_REQUIRED
        if (observeIsProUser().first()) return ChapterStudyAccessModel.OPEN
        val status = repository.fetchStatus(
            chapter = scope.chapter,
            languageCode = scope.languageCode,
        ) ?: return ChapterStudyAccessModel.OPEN
        return if (status.hasFreeStudyLeft(
                target,
            )
        ) {
            ChapterStudyAccessModel.OPEN
        } else {
            ChapterStudyAccessModel.LIMIT_REACHED
        }
    }

    private fun ChapterStudyStatusModel.hasFreeStudyLeft(target: ChapterStudyTargetModel): Boolean =
        isUnlocked || usedCount + generationCoordinator.getGeneratingCount(excluding = target) < freeLimit
}
