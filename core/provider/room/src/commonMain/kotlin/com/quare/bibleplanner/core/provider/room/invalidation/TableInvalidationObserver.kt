package com.quare.bibleplanner.core.provider.room.invalidation

import kotlinx.coroutines.flow.Flow

fun interface TableInvalidationObserver {
    operator fun invoke(vararg tables: String): Flow<Unit>
}
