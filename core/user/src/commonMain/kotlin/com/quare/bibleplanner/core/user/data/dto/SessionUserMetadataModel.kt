package com.quare.bibleplanner.core.user.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionUserMetadataModel(
    @SerialName("avatar_url") val photo: String?,
    @SerialName("name") val name: String,
)
