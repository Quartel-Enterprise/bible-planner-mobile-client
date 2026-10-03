package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.CancellationException

@Suppress("ktlint:bible-planner-style:suspend-run-catching")
inline fun <T> suspendRunCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
