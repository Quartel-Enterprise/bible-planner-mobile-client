package com.quare.bibleplanner.core.utils.jsonreader

import kotlinx.serialization.Serializable

@Serializable
internal data class ReleaseNote(
    val version: String,
    val highlights: List<String>,
)
