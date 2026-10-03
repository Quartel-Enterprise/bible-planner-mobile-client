package com.quare.bibleplanner.core.studyunlock.domain.usecase

fun interface IsRewardedUnlockEnabled {
    suspend operator fun invoke(): Boolean
}
