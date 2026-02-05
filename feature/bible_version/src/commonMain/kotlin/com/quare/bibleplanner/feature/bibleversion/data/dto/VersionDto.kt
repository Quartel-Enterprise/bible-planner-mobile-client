package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VersionDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("language") val language: String,
    @SerialName("country") val country: String,
)
