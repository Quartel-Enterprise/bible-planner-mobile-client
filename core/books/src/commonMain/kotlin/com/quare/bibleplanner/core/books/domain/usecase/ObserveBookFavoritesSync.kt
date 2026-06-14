package com.quare.bibleplanner.core.books.domain.usecase

/**
 * Long-running observer that keeps the local favorite state in sync with Supabase while a user is
 * authenticated: it pushes pending local changes, pulls remote state on (re)connection, and applies
 * real-time remote changes. Suspends forever; launch it in an app-scoped coroutine.
 */
fun interface ObserveBookFavoritesSync {
    suspend operator fun invoke()
}
