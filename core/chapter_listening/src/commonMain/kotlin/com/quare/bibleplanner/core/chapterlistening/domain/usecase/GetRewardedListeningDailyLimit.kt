package com.quare.bibleplanner.core.chapterlistening.domain.usecase

fun interface GetRewardedListeningDailyLimit {
    suspend operator fun invoke(): Int
}
