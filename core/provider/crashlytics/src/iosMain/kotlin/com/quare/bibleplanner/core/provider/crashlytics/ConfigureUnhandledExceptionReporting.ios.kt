package com.quare.bibleplanner.core.provider.crashlytics

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

internal actual fun configureUnhandledExceptionReporting() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}
