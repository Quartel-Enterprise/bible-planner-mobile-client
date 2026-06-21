package com.quare.bibleplanner.core.provider.crashlytics.domain.service

interface CrashReporter {
    fun setCollectionEnabled(enabled: Boolean)

    fun recordException(throwable: Throwable)
}
