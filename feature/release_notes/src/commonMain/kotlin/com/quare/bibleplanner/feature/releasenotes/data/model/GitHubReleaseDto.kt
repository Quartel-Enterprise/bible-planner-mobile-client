package com.quare.bibleplanner.feature.releasenotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("prerelease")
    val isPrerelease: Boolean,
    @SerialName("published_at")
    val publishedAt: String?,
    @SerialName("body")
    val body: String?,
)
