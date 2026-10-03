package com.quare.bibleplanner.core.installattribution.domain.model

data class InstallReport(
    val installId: String,
    val platform: String,
    val oppref: String?,
    val advertisingId: String?,
)
