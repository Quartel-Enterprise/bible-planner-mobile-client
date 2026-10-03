package com.quare.bibleplanner.core.installattribution.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TrackAppInstallRequestDto(
    @SerialName("install_id") val installId: String,
    @SerialName("platform") val platform: String,
    @SerialName("oppref") val oppref: String?,
    @SerialName("gaid") val gaid: String?,
)
