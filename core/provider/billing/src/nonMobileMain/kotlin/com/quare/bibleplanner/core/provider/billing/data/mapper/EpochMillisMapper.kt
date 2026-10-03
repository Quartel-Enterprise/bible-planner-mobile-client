package com.quare.bibleplanner.core.provider.billing.data.mapper

import kotlin.time.Instant

internal class EpochMillisMapper {
    fun map(isoDate: String): Long? = runCatching { Instant.parse(isoDate) }
        .getOrNull()
        ?.toEpochMilliseconds()
}
