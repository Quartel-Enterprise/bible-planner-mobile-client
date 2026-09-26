package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncVerseDto(
    @SerialName("number")
    val number: Int,
    @SerialName("text")
    val text: String,
    @SerialName("heading")
    val heading: String?,
)
