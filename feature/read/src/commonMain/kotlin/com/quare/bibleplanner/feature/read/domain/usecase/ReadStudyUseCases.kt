package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.coordinator.ChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.domain.store.PendingVerseFocusStore
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyQuota
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.PrefetchChapterStudyStatus
import com.quare.bibleplanner.core.daystudy.domain.usecase.PrefetchDayStudyQuota
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.studyunlock.domain.usecase.PrepareRewardedUnlockOffer

data class ReadStudyUseCases(
    val prefetchDayStudyQuota: PrefetchDayStudyQuota,
    val prefetchChapterStudyStatus: PrefetchChapterStudyStatus,
    val getChapterStudyAccess: GetChapterStudyAccess,
    val getChapterStudyQuota: GetChapterStudyQuota,
    val pendingVerseFocusStore: PendingVerseFocusStore,
    val chapterStudyGenerationCoordinator: ChapterStudyGenerationCoordinator,
    val prepareRewardedUnlockOffer: PrepareRewardedUnlockOffer,
    val studyUnlockResultStore: StudyUnlockResultStore,
)
