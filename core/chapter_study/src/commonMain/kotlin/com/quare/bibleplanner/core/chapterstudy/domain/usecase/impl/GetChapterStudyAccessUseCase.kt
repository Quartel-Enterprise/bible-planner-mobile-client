package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess

internal class GetChapterStudyAccessUseCase(
    private val localAccessChecker: ChapterStudyLocalAccessChecker,
    private val quotaChecker: ChapterStudyQuotaChecker,
) : GetChapterStudyAccess {
    override suspend fun invoke(target: ChapterStudyTargetModel): ChapterStudyAccessModel =
        when (val check = localAccessChecker.check(target)) {
            is ChapterStudyAccessCheck.Decided -> check.access

            is ChapterStudyAccessCheck.NeedsStatus -> getAccessFromStatus(
                target = target,
                key = check.key,
            )
        }

    private suspend fun getAccessFromStatus(
        target: ChapterStudyTargetModel,
        key: ChapterStudyStatusKey,
    ): ChapterStudyAccessModel {
        val isOpenedByPrefetchedStatus = quotaChecker.isOpenedByPrefetchedStatus(
            key = key,
            target = target,
        )
        if (isOpenedByPrefetchedStatus) return ChapterStudyAccessModel.OPEN
        val status = quotaChecker.fetchAndKeepStatus(key) ?: return ChapterStudyAccessModel.OPEN
        val hasFreeStudyLeft = quotaChecker.hasFreeStudyLeft(
            status = status,
            target = target,
        )
        return if (hasFreeStudyLeft) ChapterStudyAccessModel.OPEN else ChapterStudyAccessModel.LIMIT_REACHED
    }
}
