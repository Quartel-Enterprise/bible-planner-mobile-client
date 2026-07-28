package com.quare.bibleplanner.core.remoteconfig.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigResponseDto(
    val parameters: Map<String, String>,
)
