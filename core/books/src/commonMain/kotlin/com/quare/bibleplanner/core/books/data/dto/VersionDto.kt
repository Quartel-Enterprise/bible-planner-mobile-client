package com.quare.bibleplanner.core.books.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VersionDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("version") val version: String,
    @SerialName("language") val language: String,
    @SerialName("country") val country: String,
    @SerialName("chapters") val chapters: Int,
    @SerialName("size") val size: Long?,
)
