package com.quare.bibleplanner.core.date

import kotlin.time.Clock

fun interface CurrentTimestampProvider {
    fun getCurrentTimestamp(): Long
}

internal class DeviceClockTimestampProvider : CurrentTimestampProvider {
    override fun getCurrentTimestamp(): Long = Clock.System.now().toEpochMilliseconds()
}
