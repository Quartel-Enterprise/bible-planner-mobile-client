package com.quare.bibleplanner.feature.chapterstudy.presentation.model

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel

// Why: quota is null while logged out or when the free studies left couldn't be read,
// so isLocked treats null as unlocked.
internal data class ChapterStudyHeroUiModel(
    val isPro: Boolean,
    val quota: ChapterStudyQuotaModel?,
    val isRewardedUnlockOffered: Boolean,
) {
    val isLocked: Boolean
        get() = !isPro && quota != null && !quota.isUnlocked && quota.remainingFree == 0
}
