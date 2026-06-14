package com.quare.bibleplanner.core.user.domain.usecase

/**
 * Returns the current authenticated user id, or `null` when no user is authenticated. A one-shot read
 * for call sites that need the id once (rather than observing it), so they don't have to collect
 * [ObserveAuthenticatedUserId] themselves.
 */
fun interface GetAuthenticatedUserId {
    suspend operator fun invoke(): String?
}
