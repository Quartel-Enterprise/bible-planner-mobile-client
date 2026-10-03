package com.quare.bibleplanner.core.sync.domain.usecase

// Why: suspends forever while a user is authenticated; launch it in an app-scoped coroutine.
fun interface ObserveSync {
    suspend operator fun invoke()
}
