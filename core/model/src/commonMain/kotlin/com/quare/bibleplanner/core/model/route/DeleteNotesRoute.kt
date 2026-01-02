package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class DeleteNotesRoute(
    val readingPlanType: String,
    val week: Int,
    val day: Int,
)
