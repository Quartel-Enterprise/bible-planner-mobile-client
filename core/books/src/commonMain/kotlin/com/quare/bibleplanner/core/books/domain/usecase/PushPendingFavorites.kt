package com.quare.bibleplanner.core.books.domain.usecase

/**
 * Uploads any pending local favorite changes once. Used as a best-effort flush before logout so
 * changes made offline since the last successful sync are not lost when local data is cleared.
 * Does nothing when no user is authenticated or there is nothing pending.
 */
fun interface PushPendingFavorites {
    suspend operator fun invoke()
}
