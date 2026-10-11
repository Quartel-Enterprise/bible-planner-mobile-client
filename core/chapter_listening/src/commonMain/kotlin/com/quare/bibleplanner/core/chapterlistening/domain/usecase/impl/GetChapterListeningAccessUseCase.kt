package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningAllowanceModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId

/*
 * Why: the voices are generated on the server for a signed-in user, so listening needs an account.
 * A free user unlocks chapters with rewarded videos, or the first ones of the day without a video
 * when no video can be shown; both limits come from Remote Config, so 0 switches either off.
 */
internal class GetChapterListeningAccessUseCase(
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val isProUser: IsProUserUseCase,
    private val unlockRepository: ChapterListeningUnlockRepository,
    private val dateProvider: ListeningDateProvider,
    private val getIntRemoteConfig: GetIntRemoteConfig,
) : GetChapterListeningAccess {
    override suspend fun invoke(chapter: ChapterLocationModel): ChapterListeningAccessModel {
        if (getAuthenticatedUserId() == null) return ChapterListeningAccessModel.LoginRequired
        if (isProUser()) return ChapterListeningAccessModel.Open
        val unlockedChapters = unlockRepository.getUnlockedChapters(dateProvider.today)
        if (chapter in unlockedChapters) return ChapterListeningAccessModel.Open
        val rewardedDailyLimit = getIntRemoteConfig(
            key = REWARDED_DAILY_LIMIT_KEY,
            default = DEFAULT_REWARDED_DAILY_LIMIT,
        )
        val freeDailyLimit = getIntRemoteConfig(
            key = FREE_DAILY_LIMIT_KEY,
            default = DEFAULT_FREE_DAILY_LIMIT,
        )
        val allowance = ListeningAllowanceModel(
            hasRewardedUnlocks = rewardedDailyLimit > 0,
            hasFreeUnlocks = freeDailyLimit > 0,
        )
        val rewardedRemaining = (rewardedDailyLimit - unlockedChapters.size).coerceAtLeast(0)
        val isFreeUnlockAvailable = unlockedChapters.size < freeDailyLimit
        return if (rewardedRemaining > 0 || isFreeUnlockAvailable) {
            ChapterListeningAccessModel.UnlockAvailable(
                rewardedRemainingToday = rewardedRemaining,
                isFreeUnlockAvailable = isFreeUnlockAvailable,
                allowance = allowance,
            )
        } else {
            ChapterListeningAccessModel.LimitReached(allowance)
        }
    }

    private companion object {
        const val REWARDED_DAILY_LIMIT_KEY = "listening_rewarded_daily_limit"
        const val DEFAULT_REWARDED_DAILY_LIMIT = 1
        const val FREE_DAILY_LIMIT_KEY = "listening_free_daily_limit"
        const val DEFAULT_FREE_DAILY_LIMIT = 1
    }
}
