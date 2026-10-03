package com.quare.bibleplanner.core.provider.room.invalidation

import kotlinx.coroutines.flow.Flow

// Why: observing the signal instead of a @Query Flow lets callers throttle before paying
// for queries too expensive to re-run on every write.
fun interface TableInvalidationObserver {
    operator fun invoke(vararg tables: String): Flow<Unit>
}
