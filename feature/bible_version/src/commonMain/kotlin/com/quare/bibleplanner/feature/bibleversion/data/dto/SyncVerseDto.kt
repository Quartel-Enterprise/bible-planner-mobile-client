package com.quare.bibleplanner.feature.bibleversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncVerseDto(
    val number: Int,
    val text: String,
)
