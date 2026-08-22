package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ReadNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val isChapterRead: Boolean,
    val isFromBookDetails: Boolean,
    val weekNumber: Int? = null,
    val dayNumber: Int? = null,
    val readingPlanType: String? = null,
) : NavRoute
