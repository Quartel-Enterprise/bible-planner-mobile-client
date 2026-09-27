package com.quare.bibleplanner.feature.verse.annotations.presentation.model

internal enum class AnnotationPeriod(
    val maxDaysAgo: Int?,
    val analyticsValue: String,
) {
    ANY(
        maxDaysAgo = null,
        analyticsValue = "any",
    ),
    TODAY(
        maxDaysAgo = 0,
        analyticsValue = "today",
    ),
    LAST_7_DAYS(
        maxDaysAgo = 7,
        analyticsValue = "last_7_days",
    ),
    LAST_30_DAYS(
        maxDaysAgo = 30,
        analyticsValue = "last_30_days",
    ),
    CUSTOM(
        maxDaysAgo = null,
        analyticsValue = "custom",
    ),
}
