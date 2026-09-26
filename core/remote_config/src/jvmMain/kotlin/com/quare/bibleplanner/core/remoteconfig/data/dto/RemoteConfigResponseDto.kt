package com.quare.bibleplanner.core.remoteconfig.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigResponseDto(
    @SerialName("parameters")
    val parameters: Map<String, String>,
)
