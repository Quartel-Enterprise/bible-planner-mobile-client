package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

// Why: isCompanion marks a study opened beside the reader on a wide window, not by the user;
// a companion never generates on its own so opening the reader in landscape spends no study.
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
