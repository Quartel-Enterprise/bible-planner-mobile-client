package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class DayNavRoute(
    val dayNumber: Int,
    val weekNumber: Int,
    val readingPlanType: String,
) : NavRoute
