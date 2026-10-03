package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface Logout {
    operator fun invoke(shouldFlushPending: Boolean): Flow<LogoutProgress>
}
