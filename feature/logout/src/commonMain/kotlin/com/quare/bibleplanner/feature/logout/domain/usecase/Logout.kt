package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlinx.coroutines.flow.Flow

// Why: shouldFlushPending=false forces sign-out after a prior flush failure. Emits one
// InProgress per phase and ends with exactly one Finished; a failed flush aborts logout.
fun interface Logout {
    operator fun invoke(shouldFlushPending: Boolean): Flow<LogoutProgress>
}
