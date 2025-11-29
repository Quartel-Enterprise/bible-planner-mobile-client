package com.quare.bibleplanner.core.plan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookPlanDto(
    @SerialName("chapters") val chapters: ChaptersPlanDto? = null,
    @SerialName("name") val name: String,
)
