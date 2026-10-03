package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ChapterStudyNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val isCompanion: Boolean,
) : NavRoute

fun ReadNavRoute.toChapterStudyCompanion(): ChapterStudyNavRoute = ChapterStudyNavRoute(
    bookId = bookId,
    chapterNumber = chapterNumber,
    isCompanion = true,
)
