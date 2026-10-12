package com.quare.bibleplanner.feature.read.presentation.study

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.read.domain.usecase.ReadStudyUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class ReadChapterStudyOpener(
    private val studyUseCases: ReadStudyUseCases,
    private val navigator: Navigator,
    private val trackEvent: TrackEvent,
) {
    private var observeRewardedChapterStudyJob: Job? = null

    suspend fun open(
        scope: CoroutineScope,
        target: ChapterStudyTargetModel,
    ) {
        val access = suspendRunCatching {
            studyUseCases.getChapterStudyAccess(target)
        }.getOrDefault(ChapterStudyAccessModel.OPEN)
        when (access) {
            ChapterStudyAccessModel.OPEN -> navigator.navigate(target.toChapterStudyNavRoute())

            ChapterStudyAccessModel.LOGIN_REQUIRED -> navigator.navigate(
                LoginWarningNavRoute(LoginWarningReason.ChapterStudy.key),
            )

            ChapterStudyAccessModel.LIMIT_REACHED -> onLimitReached(
                scope = scope,
                target = target,
            )
        }
    }

    private suspend fun onLimitReached(
        scope: CoroutineScope,
        target: ChapterStudyTargetModel,
    ) {
        if (studyUseCases.chapterStudyGenerationCoordinator.hasUnservedReward(target)) {
            openRewardedChapterStudy(target)
            return
        }
        val rewardedRemainingToday = suspendRunCatching { studyUseCases.getChapterStudyQuota(target) }
            .getOrNull()
            ?.rewardedRemainingToday
            ?: 0
        if (!studyUseCases.prepareRewardedUnlockOffer(rewardedRemainingToday)) {
            navigator.navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT))
            return
        }
        val requestKey = target.toRewardedUnlockRequestKey()
        observeRewardedChapterStudyJob?.cancel()
        observeRewardedChapterStudyJob = scope.launch {
            studyUseCases.studyUnlockResultStore.observeEarned(requestKey).collect {
                openRewardedChapterStudy(target)
            }
        }
        navigator.navigate(
            StudyUnlockNavRoute(
                surface = StudyUnlockSurface.CHAPTER_STUDY,
                paywallSource = PaywallEntrySource.CHAPTER_STUDY,
                requestKey = requestKey,
                rewardedRemainingToday = rewardedRemainingToday,
                canUnlockWithoutVideo = false,
            ),
        )
    }

    private fun openRewardedChapterStudy(target: ChapterStudyTargetModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_GENERATION_STARTED,
            params = mapOf(
                AnalyticsParams.BOOK_ID to target.bookId.name,
                AnalyticsParams.CHAPTER_NUMBER to target.chapterNumber,
                AnalyticsParams.IS_PRO to false,
                AnalyticsParams.IS_REWARDED to true,
            ),
        )
        studyUseCases.chapterStudyGenerationCoordinator.start(
            target = target,
            isRewarded = true,
        )
        navigator.navigate(target.toChapterStudyNavRoute())
    }

    private fun ChapterStudyTargetModel.toChapterStudyNavRoute(): ChapterStudyNavRoute = ChapterStudyNavRoute(
        bookId = bookId.name,
        chapterNumber = chapterNumber,
        isCompanion = false,
    )

    private fun ChapterStudyTargetModel.toRewardedUnlockRequestKey(): String = listOf(
        REWARDED_UNLOCK_REQUEST_PREFIX,
        bookId.name,
        chapterNumber.toString(),
    ).joinToString(REQUEST_KEY_SEPARATOR)

    private companion object {
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "reader_chapter_study"
        const val REQUEST_KEY_SEPARATOR = "|"
    }
}
