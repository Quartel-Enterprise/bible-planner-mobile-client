package com.quare.bibleplanner.feature.chapterstudy.presentation.model

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel

/**
 * What the study beside the reader offers before it exists.
 *
 * @param quota `null` while logged out, or when the free studies left couldn't be read.
 */
internal data class ChapterStudyHeroUiModel(
    val isPro: Boolean,
    val quota: ChapterStudyQuotaModel?,
) {
    /** The free studies are used up, so generating this one needs Pro. */
    val isLocked: Boolean
        get() = !isPro && quota != null && !quota.isUnlocked && quota.remainingFree == 0
}
