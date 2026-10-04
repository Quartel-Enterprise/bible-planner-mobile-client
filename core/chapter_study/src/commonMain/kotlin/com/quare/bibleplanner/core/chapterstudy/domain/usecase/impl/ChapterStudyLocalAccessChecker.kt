package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.first

internal class ChapterStudyLocalAccessChecker(
    private val repository: ChapterStudyRepository,
    private val scopeResolver: ChapterStudyScopeResolver,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val observeIsProUser: ObserveIsProUser,
) {
    suspend fun check(target: ChapterStudyTargetModel): ChapterStudyAccessCheck {
        val scope = scopeResolver.resolve(target)
        val hasLocalStudy = repository.findCachedStudy(
            chapter = scope.chapter,
            languageCode = scope.languageCode,
        ) != null
        if (hasLocalStudy) return ChapterStudyAccessCheck.Decided(ChapterStudyAccessModel.OPEN)
        val userId = observeAuthenticatedUserId().first()
            ?: return ChapterStudyAccessCheck.Decided(ChapterStudyAccessModel.LOGIN_REQUIRED)
        if (observeIsProUser().first()) return ChapterStudyAccessCheck.Decided(ChapterStudyAccessModel.OPEN)
        return ChapterStudyAccessCheck.NeedsStatus(
            ChapterStudyStatusKey(
                userId = userId,
                scope = scope,
            ),
        )
    }
}
