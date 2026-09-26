package com.quare.bibleplanner.core.inappupdate.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItunesLookupResultDto(
    @SerialName("version") val version: String?,
)
