package com.quare.bibleplanner.feature.logout.domain.usecase

/**
 * Marks a logout that was aborted because pending changes could not be flushed first. Lets the
 * presentation layer tell this case apart from a sign-out/clear failure and handle it differently.
 */
class LogoutFlushFailedException(
    cause: Throwable,
) : Exception("Failed to flush pending changes during logout", cause)
