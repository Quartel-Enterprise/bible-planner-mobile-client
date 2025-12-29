package com.quare.bibleplanner.core.utils.date

import kotlin.time.Clock

class CurrentTimestampProvider {
    fun getCurrentTimestamp(): Long = Clock.System.now().toEpochMilliseconds()
}
