package com.quare.bibleplanner.feature.chapterstudy.presentation.model

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel

internal data class ChapterStudyHeroUiModel(
    val isPro: Boolean,
    val quota: ChapterStudyQuotaModel?,
    val isRewardedUnlockOffered: Boolean,
) {
    val isLocked: Boolean
        get() = !isPro && quota != null && !quota.isUnlocked && quota.remainingFree == 0
}
