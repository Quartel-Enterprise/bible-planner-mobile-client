package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/**
 * @param isCompanion whether the study came along with the reader on a wide window, beside the
 * chapter, rather than being opened by the user. A companion never generates on its own: opening
 * the reader in landscape must not spend a free study.
 */
@Serializable
data class ChapterStudyNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val isCompanion: Boolean = false,
) : NavRoute

fun ReadNavRoute.toChapterStudyCompanion(): ChapterStudyNavRoute = ChapterStudyNavRoute(
    bookId = bookId,
    chapterNumber = chapterNumber,
    isCompanion = true,
)
