package com.quare.bibleplanner.core.sync.domain.usecase

fun interface PushAllPending {
    suspend operator fun invoke()
}
