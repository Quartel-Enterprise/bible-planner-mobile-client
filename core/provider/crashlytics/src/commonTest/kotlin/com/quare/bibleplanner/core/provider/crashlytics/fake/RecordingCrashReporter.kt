package com.quare.bibleplanner.core.provider.crashlytics.fake

import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter

internal class RecordingCrashReporter : CrashReporter {
    val collectionEnabledValues = mutableListOf<Boolean>()
    val recordedExceptions = mutableListOf<Throwable>()

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabledValues += enabled
    }

    override fun recordException(throwable: Throwable) {
        recordedExceptions += throwable
    }
}
