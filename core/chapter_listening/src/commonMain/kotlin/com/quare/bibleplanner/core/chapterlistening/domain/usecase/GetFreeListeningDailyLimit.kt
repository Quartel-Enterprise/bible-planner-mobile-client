package com.quare.bibleplanner.core.chapterlistening.domain.usecase

fun interface GetFreeListeningDailyLimit {
    suspend operator fun invoke(): Int
}
