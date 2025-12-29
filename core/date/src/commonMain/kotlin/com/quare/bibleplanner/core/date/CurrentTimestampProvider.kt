package com.quare.bibleplanner.core.date

import kotlin.time.Clock

class CurrentTimestampProvider {
    fun getCurrentTimestamp(): Long = Clock.System.now().toEpochMilliseconds()
}

