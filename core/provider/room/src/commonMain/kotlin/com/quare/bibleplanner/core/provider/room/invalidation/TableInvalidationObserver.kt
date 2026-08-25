package com.quare.bibleplanner.core.provider.room.invalidation

import kotlinx.coroutines.flow.Flow

/**
 * Emits once per batch of writes touching any of the given tables, plus an initial emission so the
 * caller can run its query right away.
 *
 * Observing the signal instead of a `@Query` [Flow] lets the caller throttle *before* paying for the
 * query, which matters for aggregates too expensive to re-run on every write.
 */
fun interface TableInvalidationObserver {
    operator fun invoke(vararg tables: String): Flow<Unit>
}
