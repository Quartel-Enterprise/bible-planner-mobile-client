package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toListeningAnalyticsParams
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningAllowanceModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.RecordChapterListeningUnlock
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.model.StudyUnlockResultModel
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.studyunlock.domain.usecase.IsRewardedUnlockEnabled
import com.quare.bibleplanner.core.studyunlock.domain.usecase.PrepareRewardedUnlockOffer
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class ChapterListeningGate(
    private val getChapterListeningAccess: GetChapterListeningAccess,
    private val recordChapterListeningUnlock: RecordChapterListeningUnlock,
    private val prepareRewardedUnlockOffer: PrepareRewardedUnlockOffer,
    private val checkRewardedUnlockEnabled: IsRewardedUnlockEnabled,
    private val studyUnlockResultStore: StudyUnlockResultStore,
    private val navigator: Navigator,
    private val trackEvent: TrackEvent,
) {
    private var earnedJob: Job? = null

    // Why: a failed access check lets the person listen, like the chapter study does; the server's monthly budget caps what that can cost.
    fun request(
        scope: CoroutineScope,
        chapter: ChapterLocationModel,
        onAllowed: () -> Unit,
    ) {
        scope.launch {
            val access = suspendRunCatching { getChapterListeningAccess(chapter) }
                .getOrDefault(ChapterListeningAccessModel.Open)
            when (access) {
                ChapterListeningAccessModel.Open -> onAllowed()

                ChapterListeningAccessModel.LoginRequired -> navigator.navigate(
                    LoginWarningNavRoute(LoginWarningReason.ChapterListening.key),
                )

                is ChapterListeningAccessModel.UnlockAvailable -> offerUnlock(
                    scope = scope,
                    chapter = chapter,
                    access = access,
                    onAllowed = onAllowed,
                )

                is ChapterListeningAccessModel.LimitReached -> openPaywallTeaser(access.allowance)
            }
        }
    }

    // Why: without a rewarded video to offer, the first chapter of the day is free instead of going straight to the paywall.
    private suspend fun offerUnlock(
        scope: CoroutineScope,
        chapter: ChapterLocationModel,
        access: ChapterListeningAccessModel.UnlockAvailable,
        onAllowed: () -> Unit,
    ) {
        if (!prepareRewardedUnlockOffer(access.rewardedRemainingToday)) {
            if (access.isFreeUnlockAvailable) {
                unlockForFree(
                    chapter = chapter,
                    onAllowed = onAllowed,
                )
            } else {
                openPaywallTeaser(access.allowance)
            }
            return
        }
        val requestKey = listOf(
            REQUEST_KEY_PREFIX,
            chapter.bookId.name,
            chapter.chapterNumber.toString(),
        ).joinToString(REQUEST_KEY_SEPARATOR)
        earnedJob?.cancel()
        earnedJob = scope.launch {
            val result = studyUnlockResultStore.observeResult(requestKey).first()
            if (result == StudyUnlockResultModel.UNLOCKED_WITHOUT_VIDEO) trackFreeUnlock(chapter)
            recordChapterListeningUnlock(chapter)
            onAllowed()
        }
        navigator.navigate(
            StudyUnlockNavRoute(
                surface = StudyUnlockSurface.CHAPTER_LISTENING,
                paywallSource = PaywallEntrySource.CHAPTER_LISTENING,
                requestKey = requestKey,
                rewardedRemainingToday = access.rewardedRemainingToday,
                canUnlockWithoutVideo = access.isFreeUnlockAvailable,
            ),
        )
    }

    private suspend fun unlockForFree(
        chapter: ChapterLocationModel,
        onAllowed: () -> Unit,
    ) {
        trackFreeUnlock(chapter)
        recordChapterListeningUnlock(chapter)
        onAllowed()
    }

    private fun trackFreeUnlock(chapter: ChapterLocationModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_FREE_UNLOCKED,
            params = chapter.toListeningAnalyticsParams(),
        )
    }

    // Why: the teaser only speaks of the videos or the free chapter that today's configuration offers.
    private suspend fun openPaywallTeaser(allowance: ListeningAllowanceModel) {
        val isVideoUnlockOffered = allowance.hasRewardedUnlocks &&
            suspendRunCatching { checkRewardedUnlockEnabled() }.getOrDefault(false)
        val reason = when {
            isVideoUnlockOffered -> PaywallTeaserReason.CHAPTER_LISTENING_LIMIT
            allowance.hasFreeUnlocks -> PaywallTeaserReason.CHAPTER_LISTENING_FREE_LIMIT
            else -> PaywallTeaserReason.CHAPTER_LISTENING_PRO_ONLY
        }
        navigator.navigate(PaywallTeaserNavRoute(reason))
    }

    private companion object {
        const val REQUEST_KEY_PREFIX = "chapter_listening"
        const val REQUEST_KEY_SEPARATOR = "|"
    }
}
