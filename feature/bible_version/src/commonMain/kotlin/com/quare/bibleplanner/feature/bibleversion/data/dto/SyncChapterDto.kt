package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncChapterDto(
    val chapter: Int,
    val verses: List<SyncVerseDto>,
)
