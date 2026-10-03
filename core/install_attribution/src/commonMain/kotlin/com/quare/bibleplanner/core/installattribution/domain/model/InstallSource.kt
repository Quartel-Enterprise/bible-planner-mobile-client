package com.quare.bibleplanner.core.installattribution.domain.model

data class InstallSource(
    val platform: String,
    val referrer: String?,
    val advertisingId: String?,
)
