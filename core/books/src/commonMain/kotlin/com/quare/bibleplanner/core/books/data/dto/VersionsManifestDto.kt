package com.quare.bibleplanner.core.books.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VersionsManifestDto(
    @SerialName("versions") val versions: List<VersionDto>,
)
