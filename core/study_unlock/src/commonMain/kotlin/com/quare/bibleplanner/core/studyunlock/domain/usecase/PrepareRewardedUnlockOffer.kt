package com.quare.bibleplanner.core.studyunlock.domain.usecase

fun interface PrepareRewardedUnlockOffer {
    suspend operator fun invoke(rewardedRemainingToday: Int): Boolean
}
