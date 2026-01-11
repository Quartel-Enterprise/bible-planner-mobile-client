package com.quare.bibleplanner.feature.releasenotes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("body")
    val body: String? = null,
)
