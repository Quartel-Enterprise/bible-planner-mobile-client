package com.quare.bibleplanner.feature.inappupdate.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItunesLookupResponseDto(
    @SerialName("resultCount") val resultCount: Int?,
    @SerialName("results") val results: List<ItunesLookupResultDto>?,
)
