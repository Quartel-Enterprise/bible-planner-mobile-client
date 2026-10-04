package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

internal class GetChapterListeningAccessUseCase(
    private val isProUser: IsProUserUseCase,
    private val unlockRepository: ChapterListeningUnlockRepository,
    private val dateProvider: ListeningDateProvider,
    private val getIntRemoteConfig: GetIntRemoteConfig,
) : GetChapterListeningAccess {
    override suspend fun invoke(chapter: ChapterLocationModel): ChapterListeningAccessModel {
        if (isProUser()) return ChapterListeningAccessModel.Open
        val unlockedChapters = unlockRepository.getUnlockedChapters(dateProvider.today)
        if (chapter in unlockedChapters) return ChapterListeningAccessModel.Open
        val dailyLimit = getIntRemoteConfig(
            key = REWARDED_DAILY_LIMIT_KEY,
            default = DEFAULT_REWARDED_DAILY_LIMIT,
        )
        val remaining = dailyLimit - unlockedChapters.size
        return if (remaining > 0) {
            ChapterListeningAccessModel.UnlockAvailable(remaining)
        } else {
            ChapterListeningAccessModel.LimitReached
        }
    }

    private companion object {
        const val REWARDED_DAILY_LIMIT_KEY = "listening_rewarded_daily_limit"
        const val DEFAULT_REWARDED_DAILY_LIMIT = 1
    }
}
