package com.quare.bibleplanner.core.verseannotations.data.mapper

import kotlin.time.Instant

internal class SyncTimestampMapper {
    fun toIso(epochMillis: Long): String = Instant.fromEpochMilliseconds(epochMillis).toString()

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
