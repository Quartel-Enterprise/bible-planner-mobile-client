package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel

internal sealed interface ChapterStudyAccessCheck {
    data class Decided(
        val access: ChapterStudyAccessModel,
    ) : ChapterStudyAccessCheck

    data class NeedsStatus(
        val key: ChapterStudyStatusKey,
    ) : ChapterStudyAccessCheck
}
