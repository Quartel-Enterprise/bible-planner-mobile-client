package com.quare.bibleplanner.feature.logout.domain.usecase

fun interface EndSession {
    suspend operator fun invoke(): Result<Unit>
}
