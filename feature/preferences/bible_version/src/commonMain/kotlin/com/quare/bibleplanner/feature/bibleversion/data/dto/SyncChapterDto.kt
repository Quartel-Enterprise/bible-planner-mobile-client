package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncChapterDto(
    @SerialName("chapter")
    val chapter: Int,
    @SerialName("verses")
    val verses: List<SyncVerseDto>,
)
