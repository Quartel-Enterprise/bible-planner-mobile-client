package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ChapterStudyNavRoute(
    val bookId: String,
    val chapterNumber: Int,
) : NavRoute
