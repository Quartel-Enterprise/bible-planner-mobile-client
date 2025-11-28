package com.quare.bibleplanner.core.books.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChapterDataDto(
    @SerialName("number") val number: Int,
    @SerialName("verses") val verses: Int,
)
