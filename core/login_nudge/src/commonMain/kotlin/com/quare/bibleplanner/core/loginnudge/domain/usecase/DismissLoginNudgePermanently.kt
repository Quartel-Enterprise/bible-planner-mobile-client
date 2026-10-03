package com.quare.bibleplanner.core.loginnudge.domain.usecase

fun interface DismissLoginNudgePermanently {
    suspend operator fun invoke()
}
