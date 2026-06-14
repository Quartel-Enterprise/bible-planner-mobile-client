package com.quare.bibleplanner.core.user.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Emits the current authenticated user id, or `null` when no user is authenticated, re-emitting only
 * when it actually changes (sign-in / sign-out / account switch).
 */
fun interface ObserveAuthenticatedUserId {
    operator fun invoke(): Flow<String?>
}
