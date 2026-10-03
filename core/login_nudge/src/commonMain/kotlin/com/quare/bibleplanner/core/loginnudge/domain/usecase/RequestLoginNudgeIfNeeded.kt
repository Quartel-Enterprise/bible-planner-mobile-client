package com.quare.bibleplanner.core.loginnudge.domain.usecase

fun interface RequestLoginNudgeIfNeeded {
    suspend operator fun invoke()
}
