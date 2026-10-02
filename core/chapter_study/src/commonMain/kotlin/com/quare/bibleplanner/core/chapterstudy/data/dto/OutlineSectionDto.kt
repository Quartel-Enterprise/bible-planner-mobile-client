package com.quare.bibleplanner.core.chapterstudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OutlineSectionDto(
    @SerialName("startVerse") val startVerse: Int,
    @SerialName("endVerse") val endVerse: Int,
    @SerialName("title") val title: String,
)
