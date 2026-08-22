package com.quare.bibleplanner.core.verseannotations.data.mapper

import kotlin.time.Instant

/** Converts between the local epoch-millis stamps and the ISO-8601 `updated_at` the backend stores. */
internal class SyncTimestampMapper {
    fun toIso(epochMillis: Long): String = Instant.fromEpochMilliseconds(epochMillis).toString()

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
