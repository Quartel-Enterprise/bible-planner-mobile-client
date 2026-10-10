package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncBookDto(
    @SerialName("chapters")
    val chapters: List<SyncChapterDto>,
)
