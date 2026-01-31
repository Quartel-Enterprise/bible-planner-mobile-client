package com.quare.bibleplanner.feature.bibleversion.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncVerseDto(
    val number: Int,
    val text: String,
)
