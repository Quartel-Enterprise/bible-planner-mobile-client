package com.quare.bibleplanner.core.loginnudge.domain.usecase

fun interface ShouldShowLoginNudge {
    suspend operator fun invoke(): Boolean
}
