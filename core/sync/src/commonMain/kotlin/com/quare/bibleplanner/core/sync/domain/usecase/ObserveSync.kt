package com.quare.bibleplanner.core.sync.domain.usecase

/**
 * Long-running observer that keeps every registered dataset in sync with Supabase while a user is
 * authenticated: pushes pending local changes, pulls remote state on (re)connection, and applies
 * real-time remote changes. Suspends forever; launch it in an app-scoped coroutine.
 */
fun interface ObserveSync {
    suspend operator fun invoke()
}
