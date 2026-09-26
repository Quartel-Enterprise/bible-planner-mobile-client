package com.quare.bibleplanner.core.date

import kotlin.time.Clock

internal class DeviceClockTimestampProvider : CurrentTimestampProvider {
    override fun getCurrentTimestamp(): Long = Clock.System.now().toEpochMilliseconds()
}
