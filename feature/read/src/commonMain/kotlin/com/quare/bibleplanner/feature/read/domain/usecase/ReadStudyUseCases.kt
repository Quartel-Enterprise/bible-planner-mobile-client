package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.store.PendingVerseFocusStore
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess
import com.quare.bibleplanner.core.daystudy.domain.usecase.PrefetchDayStudyQuota

data class ReadStudyUseCases(
    val prefetchDayStudyQuota: PrefetchDayStudyQuota,
    val getChapterStudyAccess: GetChapterStudyAccess,
    val pendingVerseFocusStore: PendingVerseFocusStore,
)
