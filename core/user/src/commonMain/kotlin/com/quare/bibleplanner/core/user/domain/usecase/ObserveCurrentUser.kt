package com.quare.bibleplanner.core.user.domain.usecase

import com.quare.bibleplanner.core.user.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

/**
 * Emits the current authenticated [UserModel], or `null` when no user is authenticated, re-emitting
 * only when the mapped user actually changes.
 */
fun interface ObserveCurrentUser {
    operator fun invoke(): Flow<UserModel?>
}
