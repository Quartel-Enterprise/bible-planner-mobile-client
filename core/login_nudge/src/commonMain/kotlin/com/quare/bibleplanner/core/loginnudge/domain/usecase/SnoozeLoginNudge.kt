package com.quare.bibleplanner.core.loginnudge.domain.usecase

fun interface SnoozeLoginNudge {
    suspend operator fun invoke()
}
