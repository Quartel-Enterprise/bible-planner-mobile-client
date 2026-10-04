package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.RecordChapterListeningUnlock
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
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
    private val studyUnlockResultStore: StudyUnlockResultStore,
    private val navigator: Navigator,
) {
    private var earnedJob: Job? = null

    // Why: a failed access check lets the person listen, like the chapter study does, since the voice costs nothing.
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

                is ChapterListeningAccessModel.UnlockAvailable -> offerUnlock(
                    scope = scope,
                    chapter = chapter,
                    rewardedRemainingToday = access.rewardedRemainingToday,
                    onAllowed = onAllowed,
                )

                ChapterListeningAccessModel.LimitReached -> openPaywallTeaser()
            }
        }
    }

    private suspend fun offerUnlock(
        scope: CoroutineScope,
        chapter: ChapterLocationModel,
        rewardedRemainingToday: Int,
        onAllowed: () -> Unit,
    ) {
        if (!prepareRewardedUnlockOffer(rewardedRemainingToday)) {
            openPaywallTeaser()
            return
        }
        val requestKey = listOf(
            REQUEST_KEY_PREFIX,
            chapter.bookId.name,
            chapter.chapterNumber.toString(),
        ).joinToString(REQUEST_KEY_SEPARATOR)
        earnedJob?.cancel()
        earnedJob = scope.launch {
            studyUnlockResultStore.observeEarned(requestKey).first()
            recordChapterListeningUnlock(chapter)
            onAllowed()
        }
        navigator.navigate(
            StudyUnlockNavRoute(
                surface = StudyUnlockSurface.CHAPTER_LISTENING,
                paywallSource = PaywallEntrySource.CHAPTER_LISTENING,
                requestKey = requestKey,
                rewardedRemainingToday = rewardedRemainingToday,
            ),
        )
    }

    private fun openPaywallTeaser() {
        navigator.navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_LISTENING_LIMIT))
    }

    private companion object {
        const val REQUEST_KEY_PREFIX = "chapter_listening"
        const val REQUEST_KEY_SEPARATOR = "|"
    }
}
