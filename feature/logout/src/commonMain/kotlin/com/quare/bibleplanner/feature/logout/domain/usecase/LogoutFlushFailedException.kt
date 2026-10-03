package com.quare.bibleplanner.feature.logout.domain.usecase

class LogoutFlushFailedException(
    cause: Throwable,
) : Exception("Failed to flush pending changes during logout", cause)
