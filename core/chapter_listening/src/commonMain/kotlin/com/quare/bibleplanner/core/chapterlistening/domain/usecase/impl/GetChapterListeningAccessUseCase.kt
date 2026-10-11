package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetFreeListeningDailyLimit
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetRewardedListeningDailyLimit
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
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
    private val getRewardedListeningDailyLimit: GetRewardedListeningDailyLimit,
    private val getFreeListeningDailyLimit: GetFreeListeningDailyLimit,
) : GetChapterListeningAccess {
    override suspend fun invoke(chapter: ChapterLocationModel): ChapterListeningAccessModel {
        if (getAuthenticatedUserId() == null) return ChapterListeningAccessModel.LoginRequired
        if (isProUser()) return ChapterListeningAccessModel.Open
        val unlockedChapters = unlockRepository.getUnlockedChapters(dateProvider.today)
        if (chapter in unlockedChapters) return ChapterListeningAccessModel.Open
        val rewardedRemaining = (getRewardedListeningDailyLimit() - unlockedChapters.size).coerceAtLeast(0)
        val isFreeUnlockAvailable = unlockedChapters.size < getFreeListeningDailyLimit()
        return if (rewardedRemaining > 0 || isFreeUnlockAvailable) {
            ChapterListeningAccessModel.UnlockAvailable(
                rewardedRemainingToday = rewardedRemaining,
                isFreeUnlockAvailable = isFreeUnlockAvailable,
            )
        } else {
            ChapterListeningAccessModel.LimitReached
        }
    }
}
