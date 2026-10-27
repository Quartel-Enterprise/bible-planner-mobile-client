package com.quare.bibleplanner.core.plan.domain.usecase

fun interface GetReadingStreak {
    suspend operator fun invoke(): Int
}
